/*
 * Copyright (C) 2013  Camptocamp
 *
 * This file is part of MapFish Print
 *
 * MapFish Print is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MapFish Print is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MapFish Print.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mapfish.print.map.renderers.vector;

import java.util.Iterator;
import java.util.regex.Pattern;
import org.json.JSONException;
import org.json.JSONObject;
import org.mapfish.geo.MfFeature;
import org.mapfish.geo.MfGeoFactory;
import org.mapfish.geo.MfGeometry;
import org.mapfish.print.utils.PJsonObject;

/**
 * MfFactory that affects a styling object to the Features.
 */
public class StyledMfGeoFactory extends MfGeoFactory {
    /**
     * Available styles.
     */
    private PJsonObject styles;
    private String styleProperty;

    public StyledMfGeoFactory(PJsonObject styles, String styleProperty) {
        this.styles = styles;
        this.styleProperty = styleProperty;
    }

    private static final Pattern VAR_REGEXP = Pattern.compile("\\$\\{(\\w+)\\}");
    
    public MfFeature createFeature(String id, MfGeometry geometry, JSONObject properties) {
        PJsonObject style = null;
        if (styles != null) {
            JSONObject direct = properties.optJSONObject(styleProperty);
            if (direct != null) {
                style = new PJsonObject(direct, "feature.properties." + styleProperty);
            } else {
                final String styleName = properties.optString(styleProperty);
                if (styleName != null) {
                	try {
                		style = styles.getJSONObject(styleName).copy();
                    	JSONObject internal = style.getInternalObj();
                    	
                    	Iterator<String> keys = style.keys();
                    	while (keys.hasNext()) {
                    	    String key = keys.next();
                    	    String value = style.getString(key);
                    	    java.util.regex.Matcher m = VAR_REGEXP.matcher(value);
                    	    if (m.find()) {
                    	    	String attrName = m.group(1);
                    	    	String attrValue = properties.optString(attrName);
                    	    	if (attrValue != null) {
        							try {
        								internal.put(key, m.replaceFirst(attrValue));
        							} catch (JSONException e) {}
                    	    	}
                    	    }
                    	}
					} catch (JSONException e) {}
                }
            }
        }
        return new StyledMfFeature(id, geometry, style);
    }
}

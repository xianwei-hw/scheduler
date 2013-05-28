/*
 * Copyright (c) 2013 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package btrplace.json.model.view;

import btrplace.model.view.ShareableResource;
import net.minidev.json.JSONObject;

import java.util.Set;


/**
 * Serialize/Un-serialize an {@link btrplace.model.view.ShareableResource}.
 * <p/>
 * TODO: Missing default values
 *
 * @author Fabien Hermenier
 */
public class ShareableResourceConverter extends ModelViewConverter<ShareableResource> {

    @Override
    public Class<ShareableResource> getSupportedConstraint() {
        return ShareableResource.class;
    }

    @Override
    public String getJSONId() {
        return "shareableResource";
    }

    @Override
    public JSONObject toJSON(ShareableResource rc) {
        JSONObject o = new JSONObject();
        o.put("id", getJSONId());
        o.put("rcId", rc.getResourceIdentifier());

        Set<Integer> elems = rc.getDefinedVMs();
        JSONObject values = new JSONObject();
        for (int u : elems) {
            values.put(Integer.toString(u), rc.getVMConsumption(u));
        }
        o.put("vms", values);

        elems = rc.getDefinedNodes();
        values = new JSONObject();
        for (int u : elems) {
            values.put(Integer.toString(u), rc.getNodeCapacity(u));
        }
        o.put("nodes", values);

        return o;
    }

    @Override
    public ShareableResource fromJSON(JSONObject o) {
        if (!o.containsKey("id") || !o.containsKey("vms") || !o.containsKey("nodes") || !o.containsKey("rcId")) {
            return null;
        }
        String id = o.get("id").toString();
        if (!id.equals(getJSONId())) {
            return null;
        }

        String rcId = o.get("rcId").toString();

        ShareableResource rc = new ShareableResource(rcId);
        JSONObject values = (JSONObject) o.get("vms");
        for (String k : values.keySet()) {
            int u = Integer.parseInt(k);
            int v = Integer.parseInt(values.get(k).toString());
            rc.setVMConsumption(u, v);
        }
        values = (JSONObject) o.get("nodes");
        for (String k : values.keySet()) {
            int u = Integer.parseInt(k);
            int v = Integer.parseInt(values.get(k).toString());
            rc.setNodeCapacity(u, v);
        }

        return rc;
    }
}

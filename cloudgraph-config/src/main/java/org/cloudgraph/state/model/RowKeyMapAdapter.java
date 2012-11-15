package org.cloudgraph.state.model;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plasma.sdo.core.CoreConstants;

public class RowKeyMapAdapter extends XmlAdapter<RowKeyMap, HashMap<String, byte[]>>{
    private static Log log = LogFactory.getLog(RowKeyMapAdapter.class);
    private Charset charset = Charset.forName( CoreConstants.UTF8_ENCODING );

	@Override
	public HashMap<String, byte[]> unmarshal(RowKeyMap source) throws Exception {
		HashMap<String, byte[]> map = new HashMap<String, byte[]>();
		if (source != null)
			for (RowKeyMapEntry entry : source.getEntries())
				map.put(entry.getUUID(), entry.getRow().getBytes(this.charset));
		return map;
	}

	@Override
	public RowKeyMap marshal(HashMap<String, byte[]> source) throws Exception {
		RowKeyMap result = null;
		if (source != null) {
			Set<Entry<String, byte[]>> entrySet = source.entrySet();
			if (entrySet.size() > 0) {
				result = new RowKeyMap();
				for (Entry<String, byte[]> entry : entrySet) {
					RowKeyMapEntry resultEntry = new RowKeyMapEntry();
					resultEntry.setUUID(entry.getKey());
					resultEntry.setRow(new String(entry.getValue(), this.charset));
					result.getEntries().add(resultEntry);
				}
			}
		}
		return result;
	}

}

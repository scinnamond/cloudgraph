/**
 *        CloudGraph Community Edition (CE) License
 * 
 * This is a community release of CloudGraph, a dual-license suite of
 * Service Data Object (SDO) 2.1 services designed for relational and 
 * big-table style "cloud" databases, such as HBase and others. 
 * This particular copy of the software is released under the 
 * version 2 of the GNU General Public License. CloudGraph was developed by 
 * TerraMeta Software, Inc.
 * 
 * Copyright (c) 2013, TerraMeta Software, Inc. All rights reserved.
 * 
 * General License information can be found below.
 * 
 * This distribution may include materials developed by third
 * parties. For license and attribution notices for these
 * materials, please refer to the documentation that accompanies
 * this distribution (see the "Licenses for Third-Party Components"
 * appendix) or view the online documentation at 
 * <http://cloudgraph.org/licenses/>. 
 */
package org.cloudgraph.hbase.scan;

import org.cloudgraph.config.UserDefinedRowKeyFieldConfig;
import org.plasma.query.Wildcard;
import org.plasma.query.model.WildcardOperator;
import org.plasma.sdo.PlasmaType;

/**
 * A wildcard string data "flavor" specific literal class 
 * used to abstract 
 * the complexities involved in assembling the various 
 * segments and fields of composite (scan start/stop) row keys 
 * under various relational and logical operator and
 * various configurable composite key-field hashing, formatting, padding 
 * and other features.
 * 
 * @see org.cloudgraph.config.TableConfig
 * @see org.cloudgraph.hbase.service.HBaseDataConverter
 * @author Scott Cinnamond
 * @since 0.5
 */
public class WildcardStringLiteral extends StringLiteral 
    implements FuzzyRowKeyLiteral {

	private WildcardOperator wildcardOperator;

	public WildcardStringLiteral(String literal,
			PlasmaType rootType,
			WildcardOperator wildcardOperator,
			UserDefinedRowKeyFieldConfig fieldConfig) {
		super(literal, rootType, null, 
			  fieldConfig);
		this.wildcardOperator = wildcardOperator;
	}

	@Override
	public byte[] getKeyBytes() {
		byte[] keyBytes = null;
		String keyValueStr = this.literal.trim();		
		if (!fieldConfig.isHash()) {					
			if (keyValueStr.endsWith(Wildcard.WILDCARD_CHAR)) {
				keyValueStr = this.padding.back(keyValueStr, 
						this.fieldConfig.getMaxLength(), 
						Wildcard.WILDCARD_CHAR.charAt(0));
			}
			else if (keyValueStr.startsWith(Wildcard.WILDCARD_CHAR)) {
				keyValueStr = this.padding.front(keyValueStr, 
						this.fieldConfig.getMaxLength(), 
						Wildcard.WILDCARD_CHAR.charAt(0));
			}
			else {
	            throw new ScanException("cannot create fuzzy scan literal "
	                	+ "with interviening wildcards ('"+ keyValueStr +"') for row key field path '" 
	            		+ this.fieldConfig.getPropertyPath()
	                	+ "' within table "+this.table.getName()+" for graph root type, "
	                	+ this.rootType.toString());
			}
			keyBytes = keyValueStr.getBytes(this.charset);
		}
		else
            throw new ScanException("cannot create fuzzy scan literal "
            	+ "for hashed key field - field with path '" + this.fieldConfig.getPropertyPath()
            	+ "' within table "+this.table.getName()+" for graph root type, "
            	+ this.rootType.toString() + "is configured as 'hashed'");
		return keyBytes;
	}

	@Override
	public byte[] getFuzzyInfoBytes() {
		byte[] infoBytes = new byte[this.fieldConfig.getMaxLength()];
		byte[] literalChars = getKeyBytes();
		char wildcard = Wildcard.WILDCARD_CHAR.toCharArray()[0];
		for (int i = 0; i < infoBytes.length; i++)
			if (literalChars[i] != wildcard)
			    infoBytes[i] = (byte)0; // fixed char
			else
				infoBytes[i] = (byte)1; // fuzzy char
		return infoBytes;
	}
	
}

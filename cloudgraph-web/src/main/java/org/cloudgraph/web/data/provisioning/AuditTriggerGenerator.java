package org.cloudgraph.web.data.provisioning;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.apache.log4j.Logger;
import org.plasma.sdo.DataType;
import org.plasma.sdo.PlasmaProperty;
import org.plasma.sdo.PlasmaType;
import org.plasma.sdo.helper.PlasmaTypeHelper;
import org.plasma.sdo.profile.KeyType;
import org.plasma.sdo.repository.Namespace;
import org.plasma.sdo.repository.PlasmaRepository;

import org.cloudgraph.web.sdo.common.Audit;
import org.cloudgraph.web.sdo.common.Auditable;

import commonj.sdo.Property;
import commonj.sdo.Type;

public class AuditTriggerGenerator {
	
    private static Logger log = Logger.getLogger(AuditTriggerGenerator.class);
	private OutputStream os;

	public AuditTriggerGenerator(OutputStream os) throws IOException {
		this.os = os;
		
		List<Namespace> namespaces = PlasmaRepository.getInstance().getAllNamespaces();
    	
		for (Namespace namespace : namespaces) {
			if ("http://org.cloudgraph/web/sdo".equals(namespace.getUri()))
			    continue;
			log.debug("processing namespace: " + namespace.getUri());
        	List<Type> types = PlasmaTypeHelper.INSTANCE.getTypes(namespace.getUri());
        	for (Type type : types) {
        		PlasmaType plasmaType = (PlasmaType)type;
        		if (plasmaType.getPhysicalName() == null || plasmaType.getPhysicalName().length() == 0)
        			continue;
        		if (!isAuditable(plasmaType))
        			continue;
        		this.os.write(createTrigger(plasmaType).getBytes());
        	}
    	}				
	}
	
	private String createTrigger(PlasmaType auditableType) throws IOException {
		StringBuilder buf = new StringBuilder();

		List<Property> properties = auditableType.getDeclaredProperties();
		String triggerName = "TRG_" + auditableType.getPhysicalName() + "_ADT"; 
		
		PlasmaType auditType = this.getAuditType(auditableType);
		if (auditType == null)
			throw new RuntimeException("cannot find audit type for "
					+ auditableType.getURI() + "#" + auditableType.getName());
		String auditTableName = auditType.getPhysicalName();
		PlasmaProperty auditableReferenceProperty = this.getAuditableReferenceProperty(auditType); 
		
		
		buf.append("\nCREATE OR REPLACE TRIGGER FS_BAO_APPINV." + triggerName); 
		buf.append("\nAFTER UPDATE");
		buf.append("\nON FS_BAO_APPINV."+ auditableType.getPhysicalName());
		buf.append("\nREFERENCING OLD AS OLD NEW AS NEW");
		buf.append("\nFOR EACH ROW");
		buf.append("\nBEGIN");
		Boolean flag; 
		for (Property prop : properties) {
			PlasmaProperty plasmaProperty = (PlasmaProperty)prop;
			if (plasmaProperty.getPhysicalName() == null)
				continue;
			if (isManagedProperty(plasmaProperty))
				continue;
			String logicalName = plasmaProperty.getName();
			String physicalName = plasmaProperty.getPhysicalName();
			buf.append("\nIF (:OLD." + physicalName + " != :NEW." + physicalName + ")");
			buf.append("\nTHEN");
		    buf.append("\n    INSERT INTO FS_BAO_APPINV." 
					+ auditTableName 
					+ " ("
					+ auditableReferenceProperty.getPhysicalName()
					+ ", SEQ_ID, PTY_OLD_VAL, PTY_NEW_VAL, PTY_NM, PTY_PHSCL_NM, UPDT_DT, UPDT_BY_NM)"); 

			if (plasmaProperty.getType().isDataType()) {
				DataType datatype = DataType.valueOf(plasmaProperty.getType().getName()); 
				switch (datatype) {
				case String:
					if (plasmaProperty.getMaxLength() <= 255)
				        buf.append("\n    VALUES(:OLD.SEQ_ID, CTGRY_SEQ.NEXTVAL, :OLD." + physicalName + ", :NEW." + physicalName + ", '" + logicalName + "', '" + physicalName + "', SYSDATE, :NEW.LST_UPDT_BY_NM); "); 
					else 
				        buf.append("\n    VALUES(:OLD.SEQ_ID, CTGRY_SEQ.NEXTVAL, SUBSTR(:OLD." + physicalName + ",0,255), SUBSTR(:NEW." + physicalName + ",0,255), '" + logicalName + "', '" + physicalName + "', SYSDATE, :NEW.LST_UPDT_BY_NM); "); 
				    break;
				default:
				    buf.append("\n    VALUES(:OLD.SEQ_ID, CTGRY_SEQ.NEXTVAL, TO_CHAR(:OLD." + physicalName + "), TO_CHAR(:NEW." + physicalName + "), '" + logicalName + "', '" + physicalName + "', SYSDATE, :NEW.LST_UPDT_BY_NM); "); 
				    break;
				}				
			}
			else { // its a reference (i.e. FK)
			    buf.append("\n    VALUES(:OLD.SEQ_ID, CTGRY_SEQ.NEXTVAL, TO_CHAR(:OLD." + physicalName + "), TO_CHAR(:NEW." + physicalName + "), '" + logicalName + "', '" + physicalName + "', SYSDATE, :NEW.LST_UPDT_BY_NM); "); 
			}
			
			buf.append("\nEND IF;");
		}
		buf.append("\nEND;");

		return buf.toString();
	}
	
	private PlasmaType getAuditType(PlasmaType auditableType) {
		List<Property> properties = auditableType.getDeclaredProperties();
		for (Property prop : properties) {
			if (prop.getType().isDataType())
				continue; // only references
			if (isAudit(prop.getType()))
				return (PlasmaType)prop.getType();
		}
		return null;
	}

	private PlasmaProperty getAuditableReferenceProperty(PlasmaType auditType) {
		List<Property> properties = auditType.getDeclaredProperties();
		for (Property prop : properties) {
			PlasmaProperty plasmaProperty = (PlasmaProperty)prop;
			if (plasmaProperty.getType().isDataType())
				continue; // only references
			if (isAuditable(plasmaProperty.getType()))
				return plasmaProperty;
		}
		return null;
	}
	
	private boolean isManagedProperty(PlasmaProperty plasmaProperty) {
		Boolean flag; 
		flag = (Boolean)plasmaProperty.isKey(KeyType.primary);
		if (flag != null && flag.booleanValue())
			return true;
		flag = plasmaProperty.getConcurrent() != null;
		if (flag != null && flag.booleanValue())
			return true;
		
		return plasmaProperty.isKey() || plasmaProperty.isConcurrent();
	}
	
	private boolean isAuditable(Type type) {
		for (Type baseType : type.getBaseTypes())
			if (baseType.getURI().equals(Auditable.NAMESPACE_URI) &&
					baseType.getName().equals(Auditable.class.getSimpleName()))
				return true;
		return false;
	}
	
	private boolean isAudit(Type type) {
		for (Type baseType : type.getBaseTypes())
			if (baseType.getURI().equals(Audit.NAMESPACE_URI) &&
					baseType.getName().equals(Audit.class.getSimpleName()))
				return true;
		return false;
	}
	
	public static void main(String[] args) throws IOException {
		File targetFile = new File(args[0]);
		FileOutputStream os = new FileOutputStream(targetFile);
		new AuditTriggerGenerator(os);
		os.flush();
		os.close();
	}
}

package org.cloudgraph.web.query;

import org.cloudgraph.web.sdo.campaign.query.QCampaign;
import org.plasma.query.Query;

public class CampaignQuery {

	public static Query createQuery() {
		QCampaign query = QCampaign.newQuery();
		query.select(query.wildcard());
        return query;		
	}
		
	public static Query createEditQuery(Long seqId) {
		QCampaign query = QCampaign.newQuery();
		query.select(query.wildcard());
		query.where(query.seqId().eq(seqId));
        return query;		
	}
	
	public static Query createExportQuery() {
		QCampaign query = QCampaign.newQuery();
		query.select(query.wildcard());
        return query;		
	}
}

package org.cloudgraph.web.sdo.adapter;

import java.io.Serializable;
import java.util.Date;

import org.cloudgraph.web.sdo.campaign.Campaign;

public class CampaignAdapter implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private Campaign campaign;

    @SuppressWarnings("unused")
	private CampaignAdapter() {}
    
	public CampaignAdapter(Campaign campaign) {
		super();
		this.campaign = campaign;
	}
		
	public String getName() {
		return campaign.getName();
	}
	
	public String getDescription() {
		return campaign.getDescription(); 
	}
		
	public Long getId() {
		return campaign.getSeqId();
	}
	
	public String getType() {
		return campaign.getCampaignType();
	}
	
	public String getDispersal() {
		return campaign.getDispersalType();
	}
	
	public Date getStartDate() {
		return campaign.getStartDate();
	}
	
	public Date getEndDate() {
		return campaign.getEndDate();
	}
}

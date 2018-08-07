package com.nuix.investigatorreport;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SummaryInfo {
	private String tag = "";
	private String profile = "";
	private String title = "Report Title";
	private String sort = "Item Position";
	private boolean disableProductExport = false;
	
	public SummaryInfo(){}
	public SummaryInfo(String title){this.title = title;}
	
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	public String getProfile() {
		return profile;
	}
	public void setProfile(String profile) {
		this.profile = profile;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getSort() {
		return sort;
	}
	public void setSort(String sort) {
		this.sort = sort;
	}
	public Map<String,Object> toMap(){
		HashMap<String,Object> result = new HashMap<String,Object>();
		result.put("tag", tag);
		result.put("profile", profile);
		result.put("title", title);
		result.put("sort", sort);
		result.put("disable_export", disableProductExport);
		return result;
	}
	
	protected boolean isNullOrWhitespace(String value){
		if(value == null || value.trim().isEmpty())
			return true;
		else
			return false;
	}
	
	public boolean isValid(){
		if(isNullOrWhitespace(tag) || isNullOrWhitespace(title) || isNullOrWhitespace(profile) || isNullOrWhitespace(sort)){
			return false;
		}else{
			return true;
		}
	}
	
	public static boolean allAreValid(List<SummaryInfo> infos){
		for(SummaryInfo info : infos){
			if(!info.isValid())
				return false;
		}
		return true;
	}
	
	public boolean getProductExportDisabled(){ return disableProductExport; }
	public void setProductExportDisabled(boolean value){ disableProductExport = value; }
}

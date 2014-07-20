package org.lies.sortlistview;

public class SortModel {

    private String name;   //显示的数据
    private String sortLetters;  //显示数据拼音的首字母
	private String companyId ="";
	
	public String getName() {
		return name;
	}
	public String getCompanyId() {
		return companyId;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}
	public String getSortLetters() {
		return sortLetters;
	}
	public void setSortLetters(String sortLetters) {
		this.sortLetters = sortLetters;
	}
	
	
}

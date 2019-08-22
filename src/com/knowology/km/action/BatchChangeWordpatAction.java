package com.knowology.km.action;

import com.knowology.km.bll.BatchChangeWordpatDAO;

public class BatchChangeWordpatAction
{
  private String type;
  private String searchTxt;
  private String target;
  private String replace;
  private String kbdataid;
  private String brand;
  private Object m_result;

  public String execute()
  {
    if ("search".equals(this.type)) {
       this.m_result = BatchChangeWordpatDAO.search(this.brand);
     } else if ("searchByText".equals(this.type)) {
       this.searchTxt = this.searchTxt.replace("'", "");
       this.m_result = BatchChangeWordpatDAO.search(this.searchTxt, this.brand);
     } else if ("replace".equals(this.type)) {
      this.m_result = BatchChangeWordpatDAO.relpace(this.target, this.replace, this.kbdataid, this.brand);
     } else if ("addRequireWordClass".equals(this.type)) {
       this.m_result = BatchChangeWordpatDAO.addMastWordClass(this.replace, this.kbdataid, this.brand);
     } else if ("getBrand".equals(this.type)) {
       this.m_result = BatchChangeWordpatDAO.getBrand();
    }
     return "success";
  }

  public String getType() {
     return this.type;
  }

  public void setType(String type) {
     this.type = type;
  }

  public Object getM_result() {
    return this.m_result;
  }

  public void setM_result(Object m_result) {
     this.m_result = m_result;
  }

  public String getSearchTxt() {
     return this.searchTxt;
  }

  public void setSearchTxt(String searchTxt) {
    this.searchTxt = searchTxt;
  }

  public String getTarget() {
     return this.target;
  }

  public void setTarget(String target) {
     this.target = target;
  }

  public String getReplace() {
     return this.replace;
  }

  public void setReplace(String replace) {
     this.replace = replace;
  }

  public String getKbdataid() {
     return this.kbdataid;
  }

  public void setKbdataid(String kbdataid) {
     this.kbdataid = kbdataid;
  }

  public String getBrand() {
     return this.brand;
  }

  public void setBrand(String brand) {
     this.brand = brand;
  }
}

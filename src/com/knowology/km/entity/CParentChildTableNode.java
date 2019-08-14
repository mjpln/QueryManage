/**  
 * @Project: KM 
 * @Title: CParentChildTableNode.java
 * @Package com.knowology.km.entity
 * @author c_wolf your emai address
 * @date 2014-4-23 上午11:05:43
 * @Copyright: 2014 www.knowology.cn Inc. All rights reserved.
 * @version V1.0   
 */
package com.knowology.km.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * 内容摘要 ：
 *
 * 类修改者	修改日期
 * 修改说明
 * @ClassName CParentChildTableNode
 * <p>Company: knowology </p>
 * @author c_wolf your emai address
 * @date 2014-4-23 上午11:05:43
 * @version V1.0
 */

public class CParentChildTableNode {
	  public String cityids;//多个地市ID，用,分开
      public String brand;//品牌
      public String service;//业务
      public String serviceids;//多个业务ID，用,分开
      public String parentname;//父亲业务
      public List<CParentChildTableNode> children = new ArrayList<CParentChildTableNode>();
}

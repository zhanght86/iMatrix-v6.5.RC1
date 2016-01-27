package com.norteksoft.portal.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.norteksoft.acs.base.orm.hibernate.SimpleHibernateTemplate;
import com.norteksoft.acs.base.utils.ComparatorWeight;
import com.norteksoft.acs.entity.organization.Department;
import com.norteksoft.acs.entity.organization.User;
import com.norteksoft.acs.entity.organization.Workgroup;
import com.norteksoft.product.util.ContextUtils;
/*
 * 多选树时解析前台id获取需要的值
 */
@Service
@Transactional
public class TreeManager {
	private SimpleHibernateTemplate<Department, Long> departmentDao;
	private SimpleHibernateTemplate<Workgroup, Long> workGroupDao;
	private SimpleHibernateTemplate<User, Long> userDao;
	@Autowired
	public void setSessionFactory(SessionFactory sessionFactory) {
		departmentDao = new SimpleHibernateTemplate<Department, Long>(
				sessionFactory, Department.class);
		workGroupDao = new SimpleHibernateTemplate<Workgroup, Long>(
				sessionFactory, Workgroup.class);
		userDao = new  SimpleHibernateTemplate<User, Long>(
				sessionFactory, User.class);
	}
	@SuppressWarnings("unchecked")
	private List<User> getUsersByFormId(String formId){
		Map<Long,User> map=listToMapOfUser(getUsersListByFormId(formId));
		List<User> users=new ArrayList<User>();
		Iterator<Entry<Long, User>> i=map.entrySet().iterator();
		while (i.hasNext()) {
			Map.Entry<java.lang.Long, com.norteksoft.acs.entity.organization.User> entry = (Map.Entry<java.lang.Long, com.norteksoft.acs.entity.organization.User>) i
					.next();
			users.add(entry.getValue());
		}
		if(users.size()>1){
			ComparatorWeight comparator=new ComparatorWeight();
			Collections.sort(users, comparator);
		}
		return users;
	}
	@SuppressWarnings("unchecked")
	private List<User> getUsersByMap(Map<Long,User> um){
		Map<Long,User> map=um;
		List<User> users=new ArrayList<User>();
		Iterator<Entry<Long, User>> i=map.entrySet().iterator();
		while (i.hasNext()) {
			Map.Entry<java.lang.Long, com.norteksoft.acs.entity.organization.User> entry = (Map.Entry<java.lang.Long, com.norteksoft.acs.entity.organization.User>) i
					.next();
			users.add(entry.getValue());
		}
		if(users.size()>1){
			ComparatorWeight comparator=new ComparatorWeight();
			Collections.sort(users, comparator);
		}
		return users;
	}
	//String[] keys={"company","allDepartment","allWorkgroup","branch","department","user","userHasNotDepartment","hasWorkgroupBranch","workgroup"};
	//COMPANY_true:company_46;allDepartment_46;allWorkgroup_46;department_250,267;branch_265;userHasNotDepartment_46;hasWorkgroupBranch_251;user_610;workgroup_109
	private List<User> getUsersListByFormId(String formId){
		List<User> allUser=new ArrayList<User>();
		if(StringUtils.isNotEmpty(formId)){
			String[] strs=formId.split(":");
			String type=strs[0];
			String ids=strs[1];
			strs=type.split("&");
			String treeType=strs[0];
			if(!treeType.equals("MAN_DEPARTMENT_TREE")&&!treeType.equals("COMPANY")&&!treeType.equals("MAN_GROUP_TREE")){
				return new ArrayList<User>();
			}
			String userWithoutDeptVisible=strs[1];
			strs=ids.split(";");
			for(String str:strs){
				List<User> tempUser=new ArrayList<User>();
				String[] msg=str.split("_");
				String x=msg[0];
				String strIds=msg[1];
				if(x.equals("company")){
					if(!treeType.equals("MAN_GROUP_TREE")){
						if(userWithoutDeptVisible.equals("false")){
							tempUser=userDao.findList("select distinct u from DepartmentUser du join du.user u join du.department d  where du.companyId=? and du.deleted=? and u.deleted=? and d.deleted=? and du.subCompanyId <> d.id",ContextUtils.getCompanyId(),false,false,false);
						}else{
							tempUser=userDao.findList("from User u where u.companyId=? and u.deleted=?",ContextUtils.getCompanyId(),false);
						}
					}else{
						tempUser=userDao.findList("select distinct u from Workgroup wk join wk.workgroupUsers wku join wku.user u  where wk.company.id=? and wk.deleted=? and u.deleted=? and wku.deleted=?",ContextUtils.getCompanyId(),false,false,false);
					}
				}else if(x.equals("allDepartment")){
					if(userWithoutDeptVisible.equals("false")){
						tempUser=userDao.findList("select distinct u from DepartmentUser du join du.user u join du.department d  where du.companyId=? and du.deleted=? and u.deleted=? and d.deleted=? and du.subCompanyId <> d.id",ContextUtils.getCompanyId(),false,false,false);
					}else{
						tempUser=userDao.findList("select distinct u from DepartmentUser du join du.user u join du.department d  where du.companyId=? and du.deleted=? and u.deleted=? and d.deleted=?",ContextUtils.getCompanyId(),false,false,false);
					}
				}else if(x.equals("allWorkgroup")){
					tempUser=userDao.findList("select distinct u from Workgroup wk join wk.workgroupUsers wku join wku.user u  where wk.company.id=? and wku.deleted=? and wk.deleted=? and u.deleted=?",ContextUtils.getCompanyId(),false,false,false);
				}else if(x.equals("branch")){
					if(userWithoutDeptVisible.equals("false")){
						tempUser=userDao.findList("select distinct u from DepartmentUser du join du.user u join du.department d  where du.companyId=? and du.deleted=? and u.deleted=? and d.deleted=? and du.subCompanyId is not null and du.subCompanyId <> d.id and du.subCompanyId in ("+strIds+")",ContextUtils.getCompanyId(),false,false,false);
					}else{
						tempUser=userDao.findList("from User u where u.companyId=? and u.deleted=? and u.subCompanyId in ("+strIds+")",ContextUtils.getCompanyId(),false);
					}
				}else if(x.equals("department")){
					List<Department> depts=departmentDao.findList("from Department d where d.company.id=? and d.deleted=? and d.id in ("+strIds+") and d.branch=?", ContextUtils.getCompanyId(),false,false);
					tempUser=getUserByDepartment(tempUser,depts);
				}else if(x.equals("user")){
					tempUser=userDao.findList("from User u where u.companyId=? and u.deleted=? and u.id in ("+strIds+")", ContextUtils.getCompanyId(),false);
				}else if(x.equals("userHasNotDepartment")){
					String[] s=strIds.split(",");
					for(String id:s){
						if(id.equals(ContextUtils.getCompanyId().toString())){
							StringBuilder sqlString = new StringBuilder();
							sqlString.append("SELECT ACS_USER.* FROM ACS_USER LEFT OUTER JOIN ");
							sqlString.append("(SELECT * FROM ACS_DEPARTMENT_USER WHERE ACS_DEPARTMENT_USER.DELETED = 0)");
							sqlString.append(" DEPT_USER ON ACS_USER.ID = DEPT_USER.FK_USER_ID ");
							sqlString.append("WHERE ACS_USER.DELETED=0 AND ACS_USER.FK_COMPANY_ID = ? ");
							sqlString.append("AND DEPT_USER.ID IS NULL ORDER BY ACS_USER.WEIGHT DESC");
							tempUser.addAll(userDao.findByJdbc(sqlString.toString(),ContextUtils.getCompanyId()));
						}else{
							tempUser.addAll(userDao.findList("select distinct u from DepartmentUser du join du.user u join du.department d  where du.companyId=? and d.deleted=? and du.deleted=? and u.deleted=? and du.subCompanyId = d.id and du.subCompanyId=?", ContextUtils.getCompanyId(),false,false,false,Long.parseLong(id)));
						}
					}
				}else if(x.equals("hasWorkgroupBranch")){
					tempUser=userDao.findList("select distinct wu.user from WorkgroupUser wu where wu.companyId=? and wu.deleted=? and wu.workgroup.subCompanyId in ("+strIds+")", ContextUtils.getCompanyId(),false);
				}else if(x.equals("workgroup")){
					tempUser=userDao.findList("select distinct wu.user from WorkgroupUser wu where wu.companyId=? and wu.deleted=? and wu.workgroup.id in ("+strIds+")", ContextUtils.getCompanyId(),false);
				}
				allUser.addAll(tempUser);
			}
		}
		return allUser;
	}
	private List<User> getUserByDepartment(List<User> tempList,List<Department> depts){
		List<User> users=new ArrayList<User>();
		List<Department> departments=new ArrayList<Department>();
		for(Department dept:depts){
			departments=departmentDao.findList("from Department d where d.company.id=? and d.deleted=? and d.parent.id=? and d.branch=?", ContextUtils.getCompanyId(),false,dept.getId(),false);
			if(departments!=null&&departments.size()>0){
				getUserByDepartment(tempList,departments);
			}
			users=userDao.findList("select distinct u from DepartmentUser du join du.user u join du.department d  where du.companyId=? and d.deleted=? and du.deleted=? and u.deleted=? and d.id=?",ContextUtils.getCompanyId(),false,false,false,dept.getId());
			tempList.addAll(users);
		}
		return tempList;
	}
	@SuppressWarnings("unchecked")
	private List<Department> getDepartmentsByMap(Map<Long,Department> dm){
		Map<Long,Department> map=dm;
		List<Department> departments=new ArrayList<Department>();
		Iterator<Entry<Long, Department>> i=map.entrySet().iterator();
		while (i.hasNext()) {
			Map.Entry<java.lang.Long, com.norteksoft.acs.entity.organization.Department> entry = (Map.Entry<java.lang.Long, com.norteksoft.acs.entity.organization.Department>) i
					.next();
			departments.add(entry.getValue());
		}
		if(departments.size()>1){
			ComparatorWeight comparator=new ComparatorWeight();
			Collections.sort(departments, comparator);
		}
		return departments;
	}
	@SuppressWarnings("unchecked")
	private List<Department> getDepartmentsByFormId(String formId){
		Map<Long,Department> map=listToMapOfDepartment(getDepartmentsListByFormId(formId));
		List<Department> departments=new ArrayList<Department>();
		Iterator<Entry<Long, Department>> i=map.entrySet().iterator();
		while (i.hasNext()) {
			Map.Entry<java.lang.Long, com.norteksoft.acs.entity.organization.Department> entry = (Map.Entry<java.lang.Long, com.norteksoft.acs.entity.organization.Department>) i
					.next();
			departments.add(entry.getValue());
		}
		if(departments.size()>1){
			ComparatorWeight comparator=new ComparatorWeight();
			Collections.sort(departments, comparator);
		}
		return departments;
	}
	private Map<Long,Workgroup> listToMapOfWorkgroup(List<Workgroup> ws){
		Map<Long,Workgroup> map=new HashMap<Long, Workgroup>();
		for(Workgroup w:ws){
			map.put(w.getId(),w);
		}
		return map;
	}
	private Map<Long,Department> listToMapOfDepartment(List<Department> departments){
		Map<Long,Department> map=new HashMap<Long, Department>();
		for(Department d:departments){
			map.put(d.getId(),d);
		}
		return map;
	}
	private Map<Long,User> listToMapOfUser(List<User> users){
		Map<Long,User> map=new HashMap<Long, User>();
		for(User u:users){
			map.put(u.getId(),u);
		}
		return map;
	}
	private List<Department> getDepartmentsListByFormId(String formId){
		List<Department> allDepartment=new ArrayList<Department>();
		if(StringUtils.isNotEmpty(formId)){
			String[] strs=formId.split(":");
			String type=strs[0];
			String ids=strs[1];
			strs=type.split("&");
			//DEPARTMENT_WORKGROUP_TREE,DEPARTMENT_TREE,GROUP_TREE
			String treeType=strs[0];
			if(!treeType.equals("DEPARTMENT_WORKGROUP_TREE")&&!treeType.equals("DEPARTMENT_TREE")){
				return new ArrayList<Department>();
			}
//			String userWithoutDeptVisible=strs[1];
			strs=ids.split(";");
			for(String str:strs){
				List<Department> tempDepartment=new ArrayList<Department>();
				String[] msg=str.split("_");
				String x=msg[0];
				String strIds=msg[1];
				if(x.equals("company")){
					tempDepartment=departmentDao.findList("from Department d where d.company.id=? and d.deleted=?", ContextUtils.getCompanyId(),false);
				}else if(x.equals("allDepartment")){
					tempDepartment=departmentDao.findList("from Department d where d.company.id=? and d.deleted=?", ContextUtils.getCompanyId(),false);
				}else if(x.equals("branch")){
					tempDepartment=departmentDao.findList("from Department d where d.company.id=? and d.deleted=? and ((d.id in ("+strIds+") and d.branch=?) or (d.branch=? and d.subCompanyId in ("+strIds+")))", ContextUtils.getCompanyId(),false,true,false);
				}else if(x.equals("department")){
					getDepartmentByDepartment(tempDepartment,departmentDao.findList("from Department d where d.company.id=? and d.deleted=? and d.id in ("+strIds+")", ContextUtils.getCompanyId(),false));
				}else if(x.equals("realDepartment")){
					tempDepartment=departmentDao.findList("from Department d where d.company.id=? and d.deleted=? and d.id in ("+strIds+")", ContextUtils.getCompanyId(),false);
				}
				allDepartment.addAll(tempDepartment);
			}
			
		}
		return allDepartment;
	}
	private List<Department> getDepartmentByDepartment(List<Department> tempDepartment,List<Department> depts){
		List<Department> departments=new ArrayList<Department>();
		for(Department dept:depts){
			departments=departmentDao.findList("from Department d where d.company.id=? and d.deleted=? and d.parent.id=? and d.branch=?", ContextUtils.getCompanyId(),false,dept.getId(),false);
			if(departments!=null&&departments.size()>0){
				getDepartmentByDepartment(tempDepartment,departments);
			}
		}
		tempDepartment.addAll(depts);
		return tempDepartment;
	}
	private List<Workgroup> getWorkgroupsByMap(Map<Long,Workgroup> wm){
		Map<Long,Workgroup> map=wm;
		List<Workgroup> workgroups=new ArrayList<Workgroup>();
		Iterator<Entry<Long, Workgroup>> i=map.entrySet().iterator();
		while (i.hasNext()) {
			Map.Entry<java.lang.Long, com.norteksoft.acs.entity.organization.Workgroup> entry = (Map.Entry<java.lang.Long, com.norteksoft.acs.entity.organization.Workgroup>) i
					.next();
			workgroups.add(entry.getValue());
		}
		return workgroups;
	}
	private List<Workgroup> getWorkgroupsByFormId(String formId){
		Map<Long,Workgroup> map=listToMapOfWorkgroup(getWorkgroupsListByFormId(formId));
		List<Workgroup> workgroups=new ArrayList<Workgroup>();
		Iterator<Entry<Long, Workgroup>> i=map.entrySet().iterator();
		while (i.hasNext()) {
			Map.Entry<java.lang.Long, com.norteksoft.acs.entity.organization.Workgroup> entry = (Map.Entry<java.lang.Long, com.norteksoft.acs.entity.organization.Workgroup>) i
					.next();
			workgroups.add(entry.getValue());
		}
		return workgroups;
	}
	private List<Workgroup> getWorkgroupsListByFormId(String formId){
		List<Workgroup> allWorkgroup=new ArrayList<Workgroup>();
		if(StringUtils.isNotEmpty(formId)){
			String[] strs=formId.split(":");
			String type=strs[0];
			String ids=strs[1];
			strs=type.split("&");
			String treeType=strs[0];
			if(!treeType.equals("DEPARTMENT_WORKGROUP_TREE")&&!treeType.equals("GROUP_TREE")){
				return new ArrayList<Workgroup>();
			}
			//String userWithoutDeptVisible=strs[1];
			strs=ids.split(";");
			for(String str:strs){
				List<Workgroup> tempWorkgroup=new ArrayList<Workgroup>();
				String[] msg=str.split("_");
				String x=msg[0];
				String strIds=msg[1];
				if(x.equals("company")){
					tempWorkgroup=workGroupDao.findList("from Workgroup w where w.company.id=? and w.deleted=?", ContextUtils.getCompanyId(),false);
				}else if(x.equals("allWorkgroup")){
					tempWorkgroup=workGroupDao.findList("from Workgroup w where w.company.id=? and w.deleted=?", ContextUtils.getCompanyId(),false);
				}else if(x.equals("hasWorkgroupBranch")){
					tempWorkgroup=workGroupDao.findList("from Workgroup w where w.company.id=? and w.deleted=? and w.subCompanyId in ("+strIds+")", ContextUtils.getCompanyId(),false);
				}else if(x.equals("workgroup")){
					tempWorkgroup=workGroupDao.findList("from Workgroup w where w.company.id=? and w.deleted=? and w.id in ("+strIds+")", ContextUtils.getCompanyId(),false);
				}
				allWorkgroup.addAll(tempWorkgroup);
			}
		}
		return allWorkgroup;
	}
	public String getReturnValue(String formId,boolean showBranch,boolean userWithoutDeptVisible){
		boolean hasBranch = ContextUtils.hasBranch();
		List<User> users=new ArrayList<User>();
		List<Department> departments=new ArrayList<Department>();
		List<Workgroup> workgroups=new ArrayList<Workgroup>();
		String userIds="";
		String userNames="";
		String userLoginNames="";
		String userSubCompanyIds="";
		String mails="";
		String telephones="";
		String departmentIds="";
		String realDepartmentIds="";
		String realDepartmentNames="";
		String departmentNames="";
		String departmentCodes="";
		String workgroupIds="";
		String workgroupNames="";
		String departmentShortTitles="";
		if(StringUtils.isNotEmpty(formId)){
			if(formId.split("#").length>1){
				for(String fId:formId.split("#")){
					users.addAll(getUsersByFormId(fId));
					departments.addAll(getDepartmentsByFormId(fId));
					workgroups.addAll(getWorkgroupsByFormId(fId));
				}
				users=getUsersByMap(listToMapOfUser(users));
				departments=getDepartmentsByMap(listToMapOfDepartment(departments));
				workgroups=getWorkgroupsByMap(listToMapOfWorkgroup(workgroups));
				for(int i=0;i<departments.size();i++){
					departmentIds=departmentIds+departments.get(i).getId()+((i==departments.size()-1)?"":",");
					if(showBranch&&!departments.get(i).getBranch()){
						departmentNames=departmentNames+departments.get(i).getName()+"("+departments.get(i).getSubCompanyName()+")"+((i==departments.size()-1)?"":",");
					}else{
						departmentNames=departmentNames+departments.get(i).getName()+((i==departments.size()-1)?"":",");
						
					}
					departmentCodes=departmentCodes+departments.get(i).getCode()+((i==departments.size()-1)?"":",");
					departmentShortTitles=departmentShortTitles+departments.get(i).getShortTitle()+((i==departments.size()-1)?"":",");
				}
				for(int i=0;i<workgroups.size();i++){
					workgroupIds=workgroupIds+workgroups.get(i).getId()+((i==workgroups.size()-1)?"":",");
					if(showBranch&&hasBranch){
						workgroupNames=workgroupNames+workgroups.get(i).getName()+"("+workgroups.get(i).getSubCompanyName()+")"+((i==workgroups.size()-1)?"":",");
					}else{
						workgroupNames=workgroupNames+workgroups.get(i).getName()+((i==workgroups.size()-1)?"":",");
					}
				}
				for(int i=0;i<users.size();i++){
					userIds=userIds+users.get(i).getId()+((i==users.size()-1)?"":",");
					if(showBranch&&hasBranch){
						userNames=userNames+users.get(i).getName()+"("+users.get(i).getSubCompanyName()+")"+((i==users.size()-1)?"":",");
					}else{
						userNames=userNames+users.get(i).getName()+((i==users.size()-1)?"":",");
					}
					mails=mails+(users.get(i).getEmail()==null?"null":users.get(i).getEmail())+((i==users.size()-1)?"":",");
					telephones=telephones+((users.get(i).getUserInfo().getTelephone()==null||users.get(i).getUserInfo().getTelephone().equals(""))?"null":users.get(i).getUserInfo().getTelephone())+((i==users.size()-1)?"":",");
					userLoginNames=userLoginNames+users.get(i).getLoginName()+((i==users.size()-1)?"":",");
					userSubCompanyIds=userSubCompanyIds+(users.get(i).getSubCompanyId()==null?"null":users.get(i).getSubCompanyId())+((i==users.size()-1)?"":",");
				}
				for(String f:formId.split("#")){
					String[] leafstrs=f.split(":");
					String isLeafAll=isAll(leafstrs[1]);
					String leaftype=leafstrs[0];
					leafstrs=leaftype.split("&");
					//DEPARTMENT_WORKGROUP_TREE,DEPARTMENT_TREE,GROUP_TREE
					String leafTreeType=leafstrs[0];
					if(leafTreeType.equals("DEPARTMENT_WORKGROUP_TREE")){
						if(!isLeafAll.equals("")){
							if(isLeafAll.equals("company")){
								workgroupNames="所有工作组";
								departmentNames="所有部门";
							}else if(isLeafAll.equals("allDepartment")){
								departmentNames="所有部门";
							}else if(isLeafAll.equals("allWorkgroup")){
								workgroupNames="所有工作组";
							}
						}
					}
					if(leafTreeType.equals("MAN_DEPARTMENT_TREE")||leafTreeType.equals("COMPANY")||leafTreeType.equals("MAN_GROUP_TREE")){
						if((isLeafAll.equals("allDepartment")||isLeafAll.equals("company"))&&!leafTreeType.equals("MAN_GROUP_TREE")){
							userNames="所有人员";
						}
					}
					if(leafTreeType.equals("GROUP_TREE")){
						if(isLeafAll.equals("company")){
							workgroupNames="所有工作组";
						}
					}
					if(leafTreeType.equals("DEPARTMENT_TREE")){
						if(isLeafAll.equals("company")){
							departmentNames="所有部门";
						}
					}
				}
				return "multiObj={workgroup:{id:\""+workgroupIds+"\",name:\""+workgroupNames+"\"},department:{id:\""+departmentIds+"\",name:\""+departmentNames+"\",code:\""+departmentCodes+"\",shortTitle:\""+departmentShortTitles+"\"},user:{id:\""+userIds+"\",name:\""+userNames+"\",loginName:\""+userLoginNames+"\",subCompanyId:\""+userSubCompanyIds+"\",mail:\""+mails+"\",telephone:\""+telephones+"\"}}";
			}
			String[] strs=formId.split(":");
			String isAll=isAll(strs[1]);
			String type=strs[0];
			strs=type.split("&");
			//DEPARTMENT_WORKGROUP_TREE,DEPARTMENT_TREE,GROUP_TREE
			String treeType=strs[0];
			if(treeType.equals("DEPARTMENT_WORKGROUP_TREE")){
				departments=getDepartmentsByFormId(formId);
				workgroups=getWorkgroupsByFormId(formId);
				
				for(int i=0;i<departments.size();i++){
					departmentIds=departmentIds+departments.get(i).getId()+((i==departments.size()-1)?"":",");
					if(showBranch&&!departments.get(i).getBranch()&&hasBranch){
						departmentNames=departmentNames+departments.get(i).getName()+"("+departments.get(i).getSubCompanyName()+")"+((i==departments.size()-1)?"":",");
					}else{
						departmentNames=departmentNames+departments.get(i).getName()+((i==departments.size()-1)?"":",");
						
					}
					departmentCodes=departmentCodes+departments.get(i).getCode()+((i==departments.size()-1)?"":",");
					departmentShortTitles=departmentShortTitles+departments.get(i).getShortTitle()+((i==departments.size()-1)?"":",");
				}
				for(int i=0;i<workgroups.size();i++){
					workgroupIds=workgroupIds+workgroups.get(i).getId()+((i==workgroups.size()-1)?"":",");
					if(showBranch&&hasBranch){
						workgroupNames=workgroupNames+workgroups.get(i).getName()+"("+workgroups.get(i).getSubCompanyName()+")"+((i==workgroups.size()-1)?"":",");
					}else{
						workgroupNames=workgroupNames+workgroups.get(i).getName()+((i==workgroups.size()-1)?"":",");
					}
				}
				if(!isAll.equals("")){
					if(isAll.equals("company")){
						workgroupNames="所有工作组";
						departmentNames="所有部门";
					}else if(isAll.equals("allDepartment")){
						departmentNames="所有部门";
					}else if(isAll.equals("allWorkgroup")){
						workgroupNames="所有工作组";
					}
				}
				return "multiObj={workgroup:{id:\""+workgroupIds+"\",name:\""+workgroupNames+"\"},department:{id:\""+departmentIds+"\",name:\""+departmentNames+"\",code:\""+departmentCodes+"\",shortTitle:\""+departmentShortTitles+"\"}}";
			}
			
			if(treeType.equals("MAN_DEPARTMENT_TREE")||treeType.equals("COMPANY")||treeType.equals("MAN_GROUP_TREE")){
				users=getUsersByFormId(formId);
				for(int i=0;i<users.size();i++){
					userIds=userIds+users.get(i).getId()+((i==users.size()-1)?"":",");
					if(showBranch&&hasBranch){
						userNames=userNames+users.get(i).getName()+"("+users.get(i).getSubCompanyName()+")"+((i==users.size()-1)?"":",");
					}else{
						userNames=userNames+users.get(i).getName()+((i==users.size()-1)?"":",");
					}
					mails=mails+(users.get(i).getEmail()==null?"null":users.get(i).getEmail())+((i==users.size()-1)?"":",");
					telephones=telephones+((users.get(i).getUserInfo().getTelephone()==null||users.get(i).getUserInfo().getTelephone().equals(""))?"null":users.get(i).getUserInfo().getTelephone())+((i==users.size()-1)?"":",");
					userLoginNames=userLoginNames+users.get(i).getLoginName()+((i==users.size()-1)?"":",");
					userSubCompanyIds=userSubCompanyIds+(users.get(i).getSubCompanyId()==null?"null":users.get(i).getSubCompanyId())+((i==users.size()-1)?"":",");
				}
				if((isAll.equals("allDepartment")||isAll.equals("company"))&&!treeType.equals("MAN_GROUP_TREE")){
					userNames="所有人员";
				}
				if(treeType.equals("MAN_GROUP_TREE")&&isAll.equals("company")){
					userNames="所有工作组人员";
				}
				return "multiObj={user:{id:\""+userIds+"\",name:\""+userNames+"\",loginName:\""+userLoginNames+"\",subCompanyId:\""+userSubCompanyIds+"\",mail:\""+mails+"\",telephone:\""+telephones+"\"}}";
			}
			if(treeType.equals("GROUP_TREE")){
				workgroups=getWorkgroupsByFormId(formId);
				for(int i=0;i<workgroups.size();i++){
					workgroupIds=workgroupIds+workgroups.get(i).getId()+((i==workgroups.size()-1)?"":",");
					if(showBranch&&hasBranch){
						workgroupNames=workgroupNames+workgroups.get(i).getName()+"("+workgroups.get(i).getSubCompanyName()+")"+((i==workgroups.size()-1)?"":",");
					}else{
						workgroupNames=workgroupNames+workgroups.get(i).getName()+((i==workgroups.size()-1)?"":",");
					}
				}
				if(isAll.equals("company")){
					workgroupNames="所有工作组";
				}
				return "multiObj={workgroup:{id:\""+workgroupIds+"\",name:\""+workgroupNames+"\"}}";
			}
			if(treeType.equals("DEPARTMENT_TREE")){
				departments=getDepartmentsByFormId(formId);
				for(int i=0;i<departments.size();i++){
					departmentIds=departmentIds+departments.get(i).getId()+((i==departments.size()-1)?"":",");
					if(!departments.get(i).getBranch()){
						realDepartmentIds=realDepartmentIds+departments.get(i).getId()+",";
					}
					if(showBranch&&!departments.get(i).getBranch()&&hasBranch){
						departmentNames=departmentNames+departments.get(i).getName()+"("+departments.get(i).getSubCompanyName()+")"+((i==departments.size()-1)?"":",");
						if(!departments.get(i).getBranch()){
							realDepartmentNames=realDepartmentNames+departments.get(i).getName()+"("+departments.get(i).getSubCompanyName()+")"+",";
						}
					}else{
						departmentNames=departmentNames+departments.get(i).getName()+((i==departments.size()-1)?"":",");
						if(!departments.get(i).getBranch()){
							realDepartmentNames=realDepartmentNames+departments.get(i).getName()+",";
						}
					}
					departmentCodes=departmentCodes+departments.get(i).getCode()+((i==departments.size()-1)?"":",");
					departmentShortTitles=departmentShortTitles+departments.get(i).getShortTitle()+((i==departments.size()-1)?"":",");
				}
				if(isAll.equals("company")){
					departmentNames="所有部门";
				}
				if(!StringUtils.isEmpty(departmentNames)){
					if(realDepartmentNames.equals("")&&realDepartmentNames.length()>1){
						realDepartmentNames=realDepartmentNames.substring(0, realDepartmentNames.length()-1);
					}
				}
				if(!StringUtils.isEmpty(realDepartmentIds)){
					if(realDepartmentIds.equals("")&&realDepartmentIds.length()>1){
						realDepartmentIds=realDepartmentIds.substring(0, realDepartmentIds.length()-1);
					}
				}
				return "multiObj={department:{id:\""+departmentIds+"\",name:\""+departmentNames+"\",code:\""+departmentCodes+"\",shortTitle:\""+departmentShortTitles+"\",realId:\""+realDepartmentIds+"\",realName:\""+realDepartmentNames+"\"}}";
			}
		}
		return "{}";
	}
	private String isAll(String string) {
		String[] ids=string.split(";");
		String type="";
		for(String id:ids){
			type=id.split("_")[0];
			if(type.equals("company")){
				return "company";
			}
			if(type.equals("allDepartment")){
				return "allDepartment";
			}
			if(type.equals("allWorkgroup")){
				return "allWorkgroup";
			}
		}
		return "";
	}
}

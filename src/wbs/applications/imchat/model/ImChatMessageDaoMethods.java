package wbs.applications.imchat.model;

import java.util.List;

import org.hibernate.Criteria;

public
interface ImChatMessageDaoMethods {

	Criteria searchCriteria (
			ImChatMessageSearch search);

	Criteria searchOperatorReportCriteria (
			ImChatMessageSearch search);

	List<Integer> searchOperatorReportIds (
			ImChatMessageSearch search);

	List<ImChatOperatorReport> findOperatorReports (
			ImChatMessageSearch search,
			List<Integer> ids);

}

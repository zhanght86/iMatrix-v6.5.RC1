<%@ page contentType="text/html;charset=UTF-8"%>
<%@ include file="/common/mms-taglibs.jsp"%>
<html>
<head>
<title>流转历史</title>
	<%@ include file="/common/mms-iframe-meta.jsp"%>
</head>
<body>
<div class="ui-layout-center">
	<aa:zone name="button_zone"></aa:zone>
	<aa:zone name="content_zone">
	<s:if test="view">
		<table  class="form-table-border-left" >
				<thead>
					<tr>
						<th>办理人</th>
						<th>办理日期</th>
						<th>办理意见</th>
					</tr>
				</thead>
				<tbody>
					<s:iterator value="temps"> 
						<tr style="height: 22px;"><th colspan="2"> ${name}</th><th>(赞成：${yesNum}&nbsp;&nbsp;&nbsp;&nbsp;反对：${noNum} &nbsp;&nbsp;&nbsp;&nbsp;弃权：${invaNum} &nbsp;&nbsp;&nbsp;&nbsp;合计：${yesNum+noNum+invaNum})</th></tr>
						<s:if test="instanceInHistory">
							<s:iterator value="historyTask">
								<tr>
									<td width="200">${transactorName}</td>
									<td width="200"><s:date name="transactDate"  format="yyyy-MM-dd HH:mm" /></td>
									<td>
										<s:if test="taskProcessingResult.key=='agreement'">赞成</s:if><s:elseif test="taskProcessingResult.key=='oppose'">反对</s:elseif><s:else>弃权</s:else>
									</td>
								</tr>
							</s:iterator>
						</s:if><s:else>
							<s:iterator value="task">
								<tr>
									<td width="200">${transactorName}</td>
									<td width="200"><s:date name="transactDate"  format="yyyy-MM-dd HH:mm" /></td>
									<td>
										<s:if test="taskProcessingResult.key=='agreement'">赞成</s:if><s:elseif test="taskProcessingResult.key=='oppose'">反对</s:elseif><s:else>弃权</s:else>
									</td>
								</tr>
							</s:iterator>
						</s:else>
					</s:iterator>
				</tbody>
			</table>
	</s:if><s:else>${message }</s:else>
	</aa:zone>
</div>
</body>
</html>

<%--
  Created by IntelliJ IDEA.
  User: diogo
  Date: 13/12/2018
  Time: 17:13
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="s" uri="/struts-tags"%>
<html>
<head>
    <title>Regist</title>
</head>
<body>
<s:form action="regist">
    <s:textfield label="Username" name="username"/>
    <s:password label="Password" name="password"/>
    <s:submit/>
</s:form>
</body>
</html>

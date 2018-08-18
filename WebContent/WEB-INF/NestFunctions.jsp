<html>
	<%-- landing page for nest functions, get devices and set temperature --%>
	<head>
		<title>Nest</title>
	</head>	
	<body>
		<h1 align="center">Welcome to Nest Functions!</h1>
		<br/><br/>
	    <form action="NestFunction" method="get">
	    	<input type="submit" name="button" value="Get Device List" />
		</form>
		<div>
            Details : <c:out value="${Name}"></c:out>
        </div>
	    <form action="NestFunction" method="put">
	    	<input type="text" name="temperature"/>
		    <input type="submit" name="button" value="Set Temperature (farenheit)" />
		</form>
	</body>
</html>
<html>
	<!-- landing page for Ring functions get devices, turn on lights, and set do not disturb for doorbot -->
	<head>
		<title>Ring</title>
	</head>
	<body>
		<h1 align="center">Welcome to Ring Functions!</h1>
		<br/><br/>
	    <!-- buttons to get device list, onclick doGet -->
	    <form action="RingFunction" method="get">
	    	<input type="submit" name="button" value="Get Device List" />
		</form>
	    <!-- buttons to set do not disburb on doorbot-->
	    <form action="RingFunction" method="post">
	    	<input type="text" name="timeInMins"/>
		    <input type="submit" name="button" value="Set Do Not Disturb in Minutes" />
		</form>
		<!-- buttons to turn on camera lights-->
	    <form action="RingFunction" method="put">
	    	<input type="submit" name="button" value="Turn on Camera Lights" />
		</form>
	</body>
</html>
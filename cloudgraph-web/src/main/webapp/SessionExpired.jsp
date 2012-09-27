<!-- CVSFILE $Header: /CvsServer/Repository/apls/web/SessionExpired.jsp,v 1.1 2011/02/03 17:31:28 scinnamond Exp $ -->
<%@ page isErrorPage="true" %>
<% response.flushBuffer(); %>
<html>
<head>
  <title>Session Expired</title>
  <link href="css/ie_style.css" rel="stylesheet" type="text/css">
  <meta http-equiv="expires" content="now">
  <meta http-equiv="pragma" content="no-cache">
  <meta http-equiv="Cache-Control" content="no-cache">
</head>
<body bgcolor=#FFFFFF>
<div style="position:absolute; z-index:40; left:159px; top:65px; width:420px; height: 220px">
    <table width=420 border=1 cellspacing=2 cellpadding=2 class=buttons>
      <tr bgcolor=#FF6666>
        <td align=center valign=top>
          <p><font size="5">Session Expired</font></p>
        </td>
      </tr>
      <tr>
        <td>
          <table width=420 border=0 cellspacing=0 cellpadding=4 bgcolor=#CCCCCC>
            <tr>
              <td valign=middle align=center>

                <p><font size="4">
                  Your session has expired.  Click on the tab in the Siebel header to start a new session.
                </font></p>

              </td>
            </tr>
          </table>
        </td>
      </tr>
    </table>
</div>

</body>
</html>
<!-- CVSFILE $Header: /CvsServer/Repository/apls/web/SessionExpired.jsp,v 1.1 2011/02/03 17:31:28 scinnamond Exp $ -->

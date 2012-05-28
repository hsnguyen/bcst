m4_include(style.m4)
m4_define(m4_page_width,750)
m4_define(m4_menu_width,170)
m4_define(m4_menu_spacing,16)
m4_define(m4_content_width,`m4_eval(m4_page_width()-m4_menu_width()-m4_menu_spacing())')
m4_define(m4_bgcolor,`white')
m4_define(m4_darkbg,`#f0f0e0')
m4_define(m4_link_color,`#204020')
m4_define(m4_vlink_color,`#204020')
m4_define(m4_menu_color,`#902020')
m4_define(m4_menu_color2,`#202020')
m4_define(m4_menu_tac_home,`<tr><td>&nbsp;&nbsp;m4_font()<a href="http://www.sics.se/tac/" target="_top">TAC Main Page</a></td></tr>')
m4_define(m4_menu_element,`<tr><td>&nbsp;&nbsp;m4_font()<a href="$2" target=second>$1
</a></td></tr>')
m4_define(m4_menu_heading,`<tr><td bgcolor=m4_menu_color()>&nbsp;m4_font()<font color=white size=3>$1</font></td></tr>')
m4_define(m4_menu_heading2,`<tr><td>&nbsp;</td></tr><tr><td bgcolor=m4_menu_color2()>&nbsp;m4_font()<font color=white size=3>$1</font></td></tr>')
m4_define(`m4_page_begin',
`<html>
<head>
<title>TAC AgentWare for Java</title>
</head>
<body bgcolor=m4_bgcolor()
link=m4_link_color()
vlink=m4_vlink_color()>
m4_font()
')
m4_define(`m4_page_end',
`<hr>
m4_font()
<font size=1>
Copyright &copy; 2001, 2002
<a href="http://www.sics.se/">SICS AB</a>, All Rights Reserved.<br>
For more information about the TAC AgentWare 
please email <a href="mailto:tac-dev@sics.se">tac-dev@sics.se</a>.
</body>
</html>
')
m4_define(`m4_protocol_table_begin',
`<table width=600 border=0 bgcolor=#808080 cellpadding=0 cellspacing=0>
<tr><td>
<table width=600 border=0 cellspacing=1>
<tr><th width=100 bgcolor=#f0f0f0>Label</th><th width=100 bgcolor=#f0f0f0>Type</th><th bgcolor=#f0f0f0>Summary</th></tr>
')
m4_define(`m4_protocol_table_end',`</table></td></tr></table>')
m4_define(`m4_protocol_page',`m4_ifelse(`$1', commandStatus, commandstatus.html, `fields.html#$1')')
m4_define(`m4_protocol_row',
`<tr><td valign=top bgcolor=#f0f0f0><a href=m4_protocol_page($1)>$1</a></td><td valign=top bgcolor=#f0f0f0>$2</td><td valign=top bgcolor=#f0f0f0>$3</td></tr>')
m4_define(`m4_protocol_row_unused',`m4_protocol_row(`$1',`$2',`$3 (unused)')')
m4_define(`m4_protocol_row_auctionid',`m4_protocol_row(auctionID,integer,The id of the auction)')
m4_define(`m4_protocol_row_bidid',`m4_protocol_row(bidID,integer,The id of the bid)')
m4_define(`m4_protocol_row_bidstring',`m4_protocol_row(bidString,string,Specifies the buy and sell points for this bid)')
m4_define(`m4_protocol_row_bidhash',`m4_protocol_row(bidHash,string,A hash value for the bid)')
m4_define(`m4_protocol_row_rejectreason',`m4_protocol_row(rejectReason,integer,The reason for rejecting the bid)')
m4_define(`m4_protocol_row_expire',
`m4_protocol_row_unused(expireMode,integer,Specifies when a bid expires)
 m4_protocol_row_unused(expireTime,m4_timestamp(),Specifies the time a bid expires)
 m4_protocol_row_unused(divisible,integer,Specifies if a bid is divisible)')
m4_define(`m4_protocol_row_status',`m4_protocol_row(commandStatus,integer,Specifies the success or failure of this command)')
m4_define(`m4_protocol_field',`<a name=$1>m4_heading2(`$1')</a>')
m4_define(`m4_protocol_field_ignored',`<a name=$1>m4_heading2(`$1 <em><font color=red>ignored</font></em>')</a>')
m4_define(`m4_table3_begin',
`<table width=600 border=0 bgcolor=#808080 cellpadding=0 cellspacing=0>
<tr><td>
<table width=600 border=0 cellspacing=1>
<tr><th bgcolor=#f0f0f0>$1</th><th bgcolor=#f0f0f0>$2</th><th bgcolor=#f0f0f0>$3</th></tr>
')
m4_define(`m4_table3_row',
`<tr><td valign=top bgcolor=#f0f0f0>$1</td><td valign=top bgcolor=#f0f0f0>$2</td><td valign=top bgcolor=#f0f0f0>$3</td></tr>')
m4_define(`m4_table_end',`</table></td></tr></table>')
m4_define(`m4_timestamp',`<a href="timestamp.html">timestamp</a>')
m4_define(`m4_code',
`<br><font size=3><pre>m4_patsubst(`m4_patsubst(`$1',`<',`&lt;')',`>',`&gt;')</pre></font><br>')
m4_define(`m4_status_begin',`<p>Apart from the general command status codes the agent should also expect:
<dl>')
m4_define(`m4_status_row',`<dt>$1 - $2')
m4_define(`m4_status_end',`</dl>')
m4_define(`m4_warning',
`<br><table bgcolor=black border=0 cellpadding=1 cellspacing=0 width=599><tr><td><table bgcolor=ffe0e0 border=0 cellpadding=2 cellspacing=0 width=100%><tr><td>m4_font()<b>$1</b><p>$2</td></tr></table></td></tr></table><br>')
m4_define(`m4_compability',`m4_warning(Compatibility,`$1')')
m4_define(`m4_command_unimplemented',
`<br>m4_warning(Not supported,`$1 is not yet implemented in the SICS TAC server')')
m4_define(`m4_michigan',
`<a href="http://auction2.eecs.umich.edu/auction/abii_docs/html/$1.html">$2</a>')

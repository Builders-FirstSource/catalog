from com.probuild.retail.web.catalog.datasync.service import UpdateInventoryService
from com.probuild.retail.web.catalog.datasync.domain import UpdateResponse

import sys
import smtplib
from email.mime.text import MIMEText

import datetime

SMTP_SERVER = "appmail.probuild.com"

def sendEmail ( fromEmail, toList, subject, mesg ):
    SERVER = SMTP_SERVER
    #FROM = fromEmail
    #TO = toList
    #SUBJECT = subject
    #TEXT = mesg

    msg = MIMEText( mesg )
    msg['Subject'] = subject
    msg['From'] = fromEmail
    msg['To'] = toList[0]
    # Prepare actual message

    #message = """\
    #From: %s\r\nTo: %s\r\nSubject: %s\r\n\r\n%s
    #""" % (FROM, ", ".join(TO), SUBJECT, TEXT)

    # Send the mail

    server = smtplib.SMTP(SERVER)
    server.sendmail( fromEmail, toList, msg.as_string() )
    server.quit()


print "Start UpdateInventoryService - Message from InvUpdate.py"

success = 1

updateInvResponse = UpdateResponse()
updInvSrv = UpdateInventoryService()
updateInvResponse = updInvSrv.updateInventoryService()
success = updateInvResponse.result

if success != 0 :
	mesg = "Inventory Update Job failed to complete.Please verify the logs."
	sendEmail ( "jeanne.hill@probuild.com", ["prathibha.anantha@probuild.com","jeanne.hill@probuild.com","tony.bullock@probuild.com"], "Inventory Update Job FAIL Notification", mesg )
	raise Exception("Exception Occured.Please verify the logs")
else:
	mesg = "Inventory Update Job completed successfully. Items Updated: "+str(updateInvResponse.itemsUpdated)
	sendEmail ( "jeanne.hill@probuild.com", ["prathibha.anantha@probuild.com","jeanne.hill@probuild.com","tony.bullock@probuild.com"], "Inventory Update Job SUCCESS notification", mesg )
	print "End UpdateInventoryService - Messsage from InvUpdate.py"
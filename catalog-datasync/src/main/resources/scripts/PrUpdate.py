from com.probuild.retail.web.catalog.datasync.service import UpdatePriceService
from com.probuild.retail.web.catalog.datasync.domain import UpdateResponse

import sys
import smtplib
from email.mime.text import MIMEText

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


print "Start UpdatePriceService - Message from PrUpdate.py"

success = 1

updatePriceResponse = UpdateResponse()
updPrSrv = UpdatePriceService()
updatePriceResponse = updPrSrv.updatePricingService()
success = updatePriceResponse.result

if success != 0 :
	mesg = "Price Update Job failed to complete.Please verify the logs."
	sendEmail ( "jeanne.hill@probuild.com", ["prathibha.anantha@probuild.com","tony.bullock@probuild.com","jeanne.hill@probuild.com"], "Price Update Job FAIL notification", mesg )
	raise Exception("Exception Occured.Please verify the logs")
else:
	mesg = "Price Update Job completed successfully. Items Updated: "+str(updatePriceResponse.itemsUpdated)+". Items deleted: "+str(updatePriceResponse.itemsDeleted)
	sendEmail ( "jeanne.hill@probuild.com", ["prathibha.anantha@probuild.com","tony.bullock@probuild.com","jeanne.hill@probuild.com"], "Price update Job SUCCESS notification", mesg )
	print "End UpdatePriceService - Messsage from PrUpdate.py"

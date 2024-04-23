from com.probuild.retail.web.catalog.datasync.service import UpdatePdaDbService

import sys
import smtplib
#from email.mime.text import MIMEText
from email.MIMEText import MIMEText

SMTP_SERVER = "appmail.probuild.com"

def sendEmail ( fromEmail, toList, subject, mesg ):
    SERVER = SMTP_SERVER
    FROM = fromEmail
    TO = toList
    SUBJECT = subject
    TEXT = mesg

    msg = MIMEText( mesg )
    msg['Subject'] = subject
    msg['From'] = fromEmail
    msg['To'] = toList[0]
    # Prepare actual message

    message = """\
    From: %s\r\nTo: %s\r\nSubject: %s\r\n\r\n%s
    """ % (FROM, ", ".join(TO), SUBJECT, TEXT)

    # Send the mail

    server = smtplib.SMTP(SERVER)
    server.sendmail( fromEmail, toList, msg.as_string() )
    server.quit()


print "Start UpdatePdaDbService - Message from UpdatePDA.py"

updSrv = UpdatePdaDbService()    
# Linux version if updSrv.UpdatePdaDb('/QDLS/MC9190T',"jdbc:sqlite://pda.db","//", "DLCQA"):
if updSrv.UpdatePdaDb('/QDLS/MC9190',"jdbc:sqlite:C:\\pda.db","C:\\", "DRMS"):
    mesg = "PDA Update Job completed successfully." 
    sendEmail ("jeanne.hill@probuild.com",["prathibha.anantha@probuild.com","tony.bullock@probuild.com","jeanne.hill@probuild.com"], "PDA update Job SUCCESS notification", mesg )
    print "End UpdatePriceService - Messsage from PDAUpdate.py"
else:
    mesg = "PDA Update Job failed to complete.Please verify the logs."
    sendEmail ( "jeanne.hill@probuild.com", ["prathibha.anantha@probuild.com","tony.bullock@probuild.com","jeanne.hill@probuild.com"], "PDA Update Job FAIL notification", mesg )
    raise Exception("Exception Occured.Please verify the logs")


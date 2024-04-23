from com.probuild.retail.web.catalog.datasync.service import UpdatePriceService

import sys
import MailSend
from MailSend import sendEmail


#
# Module Begin
#

print "Starting UpdatePDA_DB"

# Create PDA Update service
updSrv = UpdatePdaDbService()

#
# Send mail based upon result
#
if updPrSrv.UpdatePdaDbService() != 0 :
	mesg = "PDA DB Update Job failed to complete.Please verify the logs."
	sendEmail ( "jeanne.hill@probuild.com", ["tony.bullock@probuild.com"], "PDA DB Update Job FAIL notification", mesg )
	raise Exception("Exception Occured.Please verify the logs")
else:
	mesg = "PDA DB Update Job completed successfully". Items deleted: "+str(updatePriceResponse.itemsDeleted)
	sendEmail ( "jeanne.hill@probuild.com", ["prathibha.anantha@probuild.com","tony.bullock@probuild.com","jeanne.hill@probuild.com"], "Price update Job SUCCESS notification", mesg )
	print "End UpdatePdaDbService"

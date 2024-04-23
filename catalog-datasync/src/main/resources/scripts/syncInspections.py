from javax.persistence import Persistence
from java.util import ArrayList
#from java.lang import String
from java.lang import Integer
from java.lang import Long
from java.math import BigDecimal
from jarray import array, zeros
from com.probuild.fundcontrol.bo import InspectionKey
from com.probuild.fundcontrol.bo import Inspection
from com.probuild.fundcontrol.bo import InspectionItemKey
from com.probuild.fundcontrol.bo import InspectionItem
from org.apache.commons.io import FileUtils
from org.apache.commons.io import IOUtils
from org.apache.commons.lang import RandomStringUtils
from com.probuild.fundcontrol.legacy.imaging import As400DocumentConverter
from com.probuild.fundcontrol.legacy.io import As400FileWriter
from com.probuild.fundcontrol.legacy.io import As400FileReader
from java.io import File

import sys

from com.ibm.as400.access import AS400
from com.ibm.as400.access import AS400Text
from com.ibm.as400.access import ProgramParameter
from com.ibm.as400.access import ProgramCall
#from com.ibm.as400.access import AS400Message

from java.util import Calendar
from java.text import SimpleDateFormat
from java.util import Date
from java.lang import Exception

from com.probuild.fundcontrol.inspection.ws import FundControlInspectionStub

WS_CLIENT_TOKEN = "17f750fcbfc44433912ee54404b180cb"
WS_METHOD_GET_CLOSED_INSP = "https://inspections.inspectionmanager.net/WebServices/FundControlInspection.asmx?op=GetClosedInspections"
WS_METHOD_GET_INSP_REPORT = "https://inspections.inspectionmanager.net/WebServices/FundControlInspection.asmx?op=GetInspectionReport"
TEMP_REPORT_FOLDER = "/dev/scratch"
IFS_REPORT_FOLDER = "/QDLS/DBFCWSS"

AS400_SYS = "as400.dxlbr.com"
AS400_USER = "DBFCWEB"
AS400_PASS = "W5bim@g3"

SKIP_IMAGING = False
AS400_IMAGING_PGM = "/QSYS.LIB/DBFCTEST.LIB/FCIMG120.PGM"


def getInspections ( start, stop ):
    """
    call out to inspection mgr and see if there are any new 
    inspections
    """
    
    #this is the web method we are calling
    stub = FundControlInspectionStub( WS_METHOD_GET_CLOSED_INSP )
    req = FundControlInspectionStub.GetClosedInspections()
    
    #this is the user authentication key
    req.token = WS_CLIENT_TOKEN
    
    print "Token used " + req.getToken()
    
    dateRange = FundControlInspectionStub.DateRange()
    dateRange.startDateTime = start
    dateRange.endDateTime = stop
    req.dateRange = dateRange
    
    print "start date " + sdf.format( dateRange.getStartDateTime().time )
    print "end date " + sdf.format( dateRange.getEndDateTime().time )
    return stub.getClosedInspections( req )

def getInspectionReport ( reportId ):
    """
    call out to inspection mgr and get specific report
    """

    stub = FundControlInspectionStub ( WS_METHOD_GET_INSP_REPORT )
    reqItm = FundControlInspectionStub.GetInspectionReport()
    
    reqItm.token = WS_CLIENT_TOKEN
    
    inspReportId = FundControlInspectionStub.InspectionReportId()
    inspReportId.uniqueId = reportId
    
    reqItm.inspectionReportId = inspReportId
    
    return stub.getInspectionReport( reqItm )


def createInspectionKey ( wsInspectionHeader ):
    key = InspectionKey.Builder(). \
        fund( Integer(wsInspectionHeader.fund) ). \
        suffix( (wsInspectionHeader.suffix == None and " " or wsInspectionHeader.suffix) ). \
        inspectionNumber( wsInspectionHeader.inspectionNumber ). \
        requestNumber( wsInspectionHeader.requestNumber ). \
        build()
    
    return key

def saveReportLocalCopy ( key, dataInputStream ):
    data = IOUtils.toByteArray( dataInputStream )
    fileName = "%s%s_req%s_insp%s.pdf" % (key.fund, key.suffix == " " and "x" or key.suffix, key.requestNumber, key.inspectionNumber )
    
    FileUtils.writeByteArrayToFile( File( TEMP_REPORT_FOLDER + "/" + fileName), data )


def copyReportToAS400 ( key, as400FileWriter ):
    fileName = "%s%s_req%s_insp%s.pdf" % (key.fund, key.suffix == " " and "X" or key.suffix, key.requestNumber, key.inspectionNumber )
    as400FileName = RandomStringUtils.randomAlphabetic(8)
    as400FileName = as400FileName.upper()
    #as400FileName = "%s%s%02d%03d" % (key.fund, key.suffix == " " and "X" or key.suffix, key.requestNumber, key.inspectionNumber )
    
    
    print "Copying report to AS400: " + IFS_REPORT_FOLDER+ "/" + as400FileName
    
    ### Write to AS400 IFS
    fileBytes = FileUtils.readFileToByteArray( File ( TEMP_REPORT_FOLDER + "/" + fileName ) )
    success = as400FileWriter.writeBytesToISeries( IFS_REPORT_FOLDER+ "/" + as400FileName, fileBytes )
    
    if success:
        return as400FileName
    else:
        return None

def storeReportOnAS400 ( key, as400System, as400FileName ):
    #as400FileName = "%s%s%02d%03d" % (key.fund, key.suffix == " " and "X" or key.suffix, key.requestNumber, key.inspectionNumber )
    docPath = None
    
    print "Storing report to AS400: " + IFS_REPORT_FOLDER+ "/" + as400FileName
    
    try:
        # build parameters
        param = zeros(6,ProgramParameter)
        
        param[0] = ProgramParameter( AS400Text(5).toBytes( Integer.toString(key.fund) ) ) #fund      
        param[1] = ProgramParameter( AS400Text(1).toBytes( key.suffix ) ) #suffix
        param[2] = ProgramParameter( AS400Text(6).toBytes( Integer.toString(key.requestNumber) ) ) #requestNum
        param[3] = ProgramParameter( AS400Text(6).toBytes( Integer.toString(key.inspectionNumber) ) ) #itemNum
        param[4] = ProgramParameter( AS400Text(12).toBytes( as400FileName ) ) #filename
        param[5] = ProgramParameter( 12 ) #docId returned
        
        
        programCall = ProgramCall ( as400System, AS400_IMAGING_PGM, param )
    
        if programCall.run() != True:
            msgList = programCall.getMessageList()
            
            for i in range(msgList.size()):
                print msgList[i].getText()
        
        
        textConverter = AS400Text( 12, 37, as400System )
        docPath = textConverter.toObject(param[5].getOutputData())
        print "Doc ID is: " + docPath
        
    except Exception, e:
        print "Failed to store report in AS400 imaging " + e.message
    
    
    return docPath


def createInspectionItem ( inspectionKey, wsInspectionItem, inspectionMgrId ):
    item = InspectionItem()
    
    key = InspectionItemKey()
    key.fund = inspectionKey.fund
    key.suffix = inspectionKey.suffix
    key.requestNumber = inspectionKey.requestNumber
    key.inspectionNumber = inspectionKey.inspectionNumber
    key.itemNumber = wsInspectionItem.itemNumber
    
    item.key = key
    
    item.itemDescr = wsInspectionItem.description
    item.originalBudget = BigDecimal(wsInspectionItem.originalBudget)
    item.currentBudget = BigDecimal(wsInspectionItem.currentBudget)
    item.previouslyRequested = BigDecimal(wsInspectionItem.previousBilling)
    item.requested = BigDecimal(wsInspectionItem.currentBilling)
    item.totalRequested = BigDecimal(wsInspectionItem.totalBilling)

    if wsInspectionItem.previousCompletion != None:
        item.previouslyComplete = BigDecimal(wsInspectionItem.previousCompletion)
    else:
        item.previouslyComplete = BigDecimal.ZERO
        
    if wsInspectionItem.estimatedCompletionToDate != None:
        item.complete = BigDecimal(wsInspectionItem.estimatedCompletionToDate)
    else:
        item.complete = BigDecimal.ZERO
        
    item.inspectionManagerId = inspectionMgrId
    item.inspectionManagerItemId = ""
    
    return item
    
def createInspection ( inspectionKey, wsInspection ):
    """
    Create an AS400 inspection record
    """
    sdf = SimpleDateFormat ( "yyyyMMdd" )
    
    insp = Inspection()
    insp.key = inspectionKey
    
    insp.closedDate = wsInspection.closedDate
    insp.legacyClosedDate = Integer( sdf.format( insp.closedDate.time ) )
    
    insp.openedDate = wsInspection.openDate
    insp.legacyOpenedDate = Integer( sdf.format( insp.openedDate.time ) )
    
    insp.scheduledDate = wsInspection.scheduleDate
    insp.legacyScheduledDate = Integer( sdf.format( insp.scheduledDate.time ) )
    
    insp.date = Calendar.getInstance()
    insp.legacyDate = Integer ( sdf.format( insp.date.time ) )
    
    insp.inspectorId = wsInspection.inspectorId
    insp.inspectorName = wsInspection.inspectorName
    
    insp.status = "Closed"
    
    if wsInspection.previousEstimatedCompletion != None:
        insp.previouslyComplete = BigDecimal(wsInspection.previousEstimatedCompletion)
    else:
        insp.previouslyComplete = BigDecimal.ZERO
        
    insp.previouslyCompleteDate  = Integer( sdf.format(wsInspection.previousEstimatedCompletionDate.time) )
    
    
    if wsInspection.estimatedCompletion != None:
        insp.complete = BigDecimal(wsInspection.estimatedCompletion)
    else:
        insp.complete = BigDecimal.ZERO 
    
    insp.completeDate  = Integer( sdf.format(wsInspection.estimatedCompletionDate.time) )
    
    insp.notes = ""
    insp.inspectionManagerId = wsInspection.inspectionReportId.uniqueId
    insp.reportArchived = "N"
    insp.documentKey = ""
    
    items = ArrayList()
    
    for i in range( len(wsInspection.budgetItems.item ) ):
        item = createInspectionItem( key, wsInspection.budgetItems.item[i], insp.inspectionManagerId )
        items.add( item )
    
    insp.items = items
    
    saveReportLocalCopy ( inspectionKey, wsInspection.pdfReport.inputStream )
    
    return insp

def inspectionReportExists ( inspectionKey ):
    success = False
    #try:
    #factory = Persistence.createEntityManagerFactory( "as400Test" )
    em = factory.createEntityManager()

    insp = em.find(Inspection, inspectionKey)
    if insp != None:
        success = True
    
    em.close()

    #except:
    #    print "Error checking if report exists"
        
    return success

# global setup
factory = Persistence.createEntityManagerFactory( "as400Test" )


sdf = SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" )
begin = Calendar.getInstance()
end = Calendar.getInstance()

begin.setTimeInMillis ( sdf.parse( "2010-08-02 00:00:00" ).time )
end.setTimeInMillis ( sdf.parse ( "2010-08-02 23:59:59" ).time )

res = getInspections ( begin, end )

if not res.item:
    print "Not items to process"
    sys.exit()
    
print "items returned %d" % len( res.item )


# get a database connection going
em = factory.createEntityManager()

# connect to as400
print "Connecting to AS400"
as400 = AS400( AS400_SYS, AS400_USER, AS400_PASS )

writer = As400FileWriter()
writer.system = as400

# loop through each report returned
for i in range ( len(res.item) ):
    key = createInspectionKey ( res.getItem()[i] )
    
    reportExists = inspectionReportExists ( key )
    #print "inspection report exists %s" % reportExists

    if reportExists:
        print "skipped report Fund: %s%s; Req#: %s; Insp#: %s because it already exists" % (key.fund, key.suffix, key.requestNumber, key.inspectionNumber)
        continue
    
    #get the report details
    print "fetch report %s" % res.getItem()[i].getInspectionReportId().uniqueId
    reportRes = getInspectionReport ( res.getItem()[i].getInspectionReportId().uniqueId )

    print "Report returned %s" % reportRes.item.fund

    inspRec = createInspection(key, reportRes.getItem() )


    print "Store inspection record"
    em.transaction.begin()
    em.persist ( inspRec )
    
    #get the image (PDF report) and transfer to AS400
    as400FileName = copyReportToAS400 ( key, writer )
    if as400FileName == None:
        print "Skipping report, due to previous error"
        em.transaction.rollback()
        continue;
    
    # store image in legacy imaging
    if not SKIP_IMAGING:
        #add to as400
        docId = storeReportOnAS400 ( key, as400, as400FileName )
        if docId == None:
            print "Skipping report, due to previous error"
            em.transaction.rollback()
            continue
        
        inspRec.documentKey = docId
        inspRec.reportArchived = "Y"
        
    #if image store did not work, abort transaction
    em.transaction.commit()
    
    
em.close

as400.disconnectAllServices()


#for i in range( len(res.item) ):
#    print res.getItem()[i].inspectionReportId.uniqueId
#    print res.getItem()[i].fund
#    print res.getItem()[i].suffix
#    print res.getItem()[i].requestNumber
#    print res.getItem()[i].inspectionNumber
#    print sdf.format(res.getItem()[i].closedDate.time)
#    print "- + - + - + - + - + - + - + -"

#documentId = 'B10295AA.XYZ'
#
### build parameters
#docId = AS400Text(12)
#
#param = zeros(2,ProgramParameter)
#
#param[0] = ProgramParameter ( docId.toBytes( documentId ) )
#param[1] = ProgramParameter ( 35 )
#
#as400 = AS400( 'as400.dxlbr.com', 'DBFCWEB', 'W5bim@g3' )
#print 'Logged in OK'
#
#programCall = ProgramCall ( as400, '/QSYS.LIB/DBFCIE.LIB/FCIMG100.PGM', param )
##programCall = ProgramCall ( as400, '/QSYS.LIB/PGMR9.LIB/FCIMG100.PGM', param )
#print 'Created program call'
#
#print 'Calling program'
#if programCall.run() != True:
#    msgList = programCall.getMessageList()
#    
#    for i in range(msgList.size()):
#        print msgList[i].getText()
#
##print out path
#textConverter = AS400Text( 35, 37, as400 )
#docPath = textConverter.toObject(param[1].getOutputData())
#print "Path is: " + docPath
#
#as400.disconnectAllServices()


# build parameters
#docId = AS400Text(12)
#
#param = zeros(6,ProgramParameter)
#
#param[0] = ProgramParameter( AS400Text(5).toBytes( '12345' ) ) #fund      
#param[1] = ProgramParameter( AS400Text(1).toBytes( ' ' ) ) #suffix
##param[2] = ProgramParameter( AS400Text(9).toBytes( '0' ) ) #vendorId
#param[2] = ProgramParameter( AS400Text(6).toBytes( '0' ) ) #requestNum
#param[3] = ProgramParameter( AS400Text(6).toBytes( '0' ) ) #itemNum
##param[5] = ProgramParameter( AS400Text(45).toBytes( '' ) ) #vendor name;
##param[6] = ProgramParameter( AS400Text(54).toBytes( '' ) ) #itemDesc
##param[7] = ProgramParameter( AS400Text(12).toBytes( '0' ) ) #itemAmt
##param[8] = ProgramParameter( AS400Text(40).toBytes( '' ) ) #issuer
##param[9] = ProgramParameter( AS400Text(8).toBytes( 'FCCNTRCT' ) ) #doc type
##param[10] = ProgramParameter( AS400Text(40).toBytes( 'DBFCWEB' ) ) #user
#param[4] = ProgramParameter( AS400Text(12).toBytes( 'SAMPLE' ) ) #filename
#param[5] = ProgramParameter( 12 ) #docId returned
##param[12] = ProgramParameter( AS400Text(12).toBytes( 'SAMPLE' ) ) #filename
##param[13] = ProgramParameter( AS400Text(9).toBytes( '0' ) ) #checkNum
##param[14] = ProgramParameter( AS400Text(9).toBytes( '0' ) ) #receiptNum
##param[15] = ProgramParameter( AS400Text(9).toBytes( '0' ) ) #voucherNum
##param[0] = ProgramParameter ( docId.toBytes( documentId ) )
##param[1] = ProgramParameter ( 35 )
#
#as400 = AS400( 'as400.dxlbr.com', 'DBFCWEB', 'W5bim@g3' )
#print 'Logged in OK'
#
#
#programCall = ProgramCall ( as400, '/QSYS.LIB/DBFCTEST.LIB/FCIMG120.PGM', param )
#print 'Created program call'
#
#print 'Calling program'
#if programCall.run() != True:
#    msgList = programCall.getMessageList()
#    
#    for i in range(msgList.size()):
#        print msgList[i].getText()
#
##print out path
#textConverter = AS400Text( 12, 37, as400 )
#docPath = textConverter.toObject(param[5].getOutputData())
#print "Doc ID is: " + docPath
#
#as400.disconnectAllServices()


# fetch file and bring it over
#reader = As400FileReader()
#reader.connectToAS400( 'as400.dxlbr.com', 'DBFCWEB', 'W5bim@g3' )
#fileBytes = reader.readBytesFromFile ( "/QDLS" + docPath )
#FileUtils.writeByteArrayToFile( File ( "c:/dev/scratch/" + documentId + ".pdf" ), fileBytes )
#reader.disconnectToAS400()
#
#fileBytes = FileUtils.readFileToByteArray( File ( "c:/dev/scratch/" + documentId + ".mod" ) )
#converter = As400DocumentConverter()
#pdfBytes = converter.convert ( fileBytes, As400DocumentConverter.PDF, True )
#FileUtils.writeByteArrayToFile ( File( "c:/dev/scratch/" + documentId + ".pdf" ), pdfBytes )


#print "hello world!"
#
#factory = Persistence.createEntityManagerFactory( "as400Test" )
#em = factory.createEntityManager()
#
#
#key = InspectionKey.Builder(). \
#        fund(12345).suffix(" "). \
#        inspectionNumber(0). \
#        requestNumber(0).build()
#
#
#insp = em.find(Inspection, key)
#
#print "Items found %s" % (insp.items.size())
#
#em.close()


#fileBytes = FileUtils.readFileToByteArray( File ( "c:/dev/scratch/sample_test.pdf" ) )

#converter = As400DocumentConverter()

#modcaBytes = converter.convert ( fileBytes, As400DocumentConverter.MODCA, True )

#FileUtils.writeByteArrayToFile ( File( "c:/dev/scratch/sample_test.mod" ), modcaBytes )


### Write to AS400 IFS
#writer = As400FileWriter()
#writer.connectToAS400( "as400.dxlbr.com", "SATCP", "SATCP" )
#fileBytes = FileUtils.readFileToByteArray( File ( "c:/dev/scratch/15806_7.pdf" ) )
#writer.writeBytesToISeries( "/opt/fcimg/inspection.pdf", fileBytes )
#writer.disconnectToAS400()

#reader = As400FileReader()
#reader.connectToAS400( "as400.dxlbr.com", "SATCP", "SATCP" )
#fileBytes = reader.readBytesFromFile ( "/opt/fcimg/inspection.pdf" )
#FileUtils.writeByteArrayToFile( File ( "c:/dev/scratch/inspFromAs400.pdf" ), fileBytes )
#reader.disconnectToAS400()



#result = em.createNativeQuery( "Select flname, fladr1 from dbfc.fclendfl order by flname" ).getResultList()

#print result.size()

#for i in range( result.size() ):
#    rs = array(result.get(i), String)
#    cols = ArrayList( rs.size )
#    for i in range( rs.size ):
#        cols.add ( rs[i] )
#    print rs

#Lifecycle.endCall()
#print sys
#a = 42
#print a
#x = 2 + 2
#print "x:",x
from com.probuild.retail.web.catalog.repository import AS400ItemRepository
from com.probuild.retail.web.catalog.domain import Item
from com.probuild.retail.web.catalog.datasync.service import DashboardService
from org.jboss.seam import Component
from org.jboss.seam.contexts import Lifecycle

import sys

print "connecting to item repository"

repository = AS400ItemRepository()
print "connected: ",repository.connect()

item = repository.findItem( "100036" )
print item.name
print item.descr

repository.disconnect()

#Lifecycle.beginCall() # this is called outside the script
dashboardService = Component.getInstance( "dashboardService", 1 )
jobs = dashboardService.getAllJobs()

for job in jobs:
    print job.jobName

#Lifecycle.endCall()
#print sys
#a = 42
#print a
#x = 2 + 2
#print "x:",x
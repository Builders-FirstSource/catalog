from com.probuild.retail.web.catalog.repository import AS400ItemRepository

from com.probuild.retail.web.catalog.domain import Item

#from com.probuild.retail.web.catalog.datasync.service import DashboardService

from org.jboss.seam import Component

from org.jboss.seam.contexts import Lifecycle

from com.caucho.hessian.client import HessianProxyFactory

from com.probuild.retail.web.catalog.ext.service import WebCatalogService

from java.lang import Integer

import sys

import datetime



def scanForChildren ( groups ):

    global catalogService



    for grp in groups:

        scanForItems ( grp )



    return



def scanForItems ( group ):

    global catalogService # access to catalog



    print "Processing GROUP: ",group.name



    # try to get subgroups

    subGroups = catalogService.findAllChildCategories(group.id)

    #print "subgroup count ",len(subGroups)

    if len(subGroups) > 0:

        scanForChildren ( subGroups )

    else:

        items = catalogService.findProductsForCategory(group.id)

        updatePrices ( items )



    return



def updatePrices ( items ):

    global repository # access to source item repository

    global catalogService



    for itm in items:



	x = itm.name

	x = x.encode('utf-8')

	y = unicode(x,'utf-8')

        print "    Updating ",repr(y),

        rItem = repository.findItem ( Integer.toString(itm.sku) )



        if rItem == None:

            print "!!!Item not found in repository, deleting "

            catalogService.removeItem(itm)

            continue



        itm.regularPrice = rItem.regularPrice



        # only set sale price if not zero

        if rItem.salePrice.doubleValue() == 0:

            itm.salePrice = rItem.regularPrice

        else:

            itm.salePrice = rItem.salePrice



        catalogService.saveItem ( itm )



        print "...saved item ",repr(y)



    return





print "- - - - - Updating Prices - - - - - -"



print " >>>>> Update Price Start Time <<<<<<"

print datetime.datetime.now()



print "Connecting to AS400...",

repository = AS400ItemRepository()

print "connected: ",repository.connect()



print "Connection to Catalog...",

hessianFactory = HessianProxyFactory()

hessianFactory.user = "jdoe"

hessianFactory.password = "foo"

catalogService = hessianFactory.create( WebCatalogService, "http://localhost:8080/catalog/services/CatalogService" )

print "connected: ", not catalogService.imageExists( "", "cat" )



# start the scan with the parents

parents = catalogService.findAllChildCategories(1)

scanForChildren ( parents )



print "- - - - - Update Complete - - - - - -"



print " >>>>> Update Price End Time <<<<<<"

print datetime.datetime.now()



#item = repository.findItem( "100036" )

#print item.name

#print item.descr



repository.disconnect()



#Lifecycle.beginCall() # this is called outside the script

#dashboardService = Component.getInstance( "dashboardService", 1 )

#jobs = dashboardService.getAllJobs()



#for job in jobs:

#    print job.jobName



#Lifecycle.endCall()

#print sys

#a = 42

#print a

#x = 2 + 2

#print "x:",x


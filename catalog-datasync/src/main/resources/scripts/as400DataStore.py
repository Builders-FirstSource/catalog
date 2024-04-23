'''
Created on Aug 30, 2010

@author: jsimmons

Provides general access to the AS400 tables via Hibernate
'''

from javax.persistence import Persistence


def find ( obj, key ):
    factory = Persistence.createEntityManagerFactory( "as400Test" )
    
    em = factory.createEntityManager()
    
    found = em.find( obj, key )
    
    em.close();
    return em.find( obj, key )
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
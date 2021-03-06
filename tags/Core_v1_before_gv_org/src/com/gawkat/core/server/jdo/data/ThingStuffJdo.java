package com.gawkat.core.server.jdo.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.jdo.Extent;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.Transaction;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.NotPersistent;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.gawkat.core.client.account.thing.ThingData;
import com.gawkat.core.client.account.thingstuff.ThingStuffData;
import com.gawkat.core.client.account.thingstuff.ThingStuffFilterData;
import com.gawkat.core.client.account.thingstuff.ThingStuffsData;
import com.gawkat.core.server.ServerPersistence;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable="true")
public class ThingStuffJdo {

	@NotPersistent
	private ServerPersistence sp = null;

	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Key thingStuffIdKey;

	// why kind of stuff, defined as type
	@Persistent
	private long thingStuffTypeId;

	// who is the owner
	@Persistent
	private long thingId;

	// values that can be stored
	@Persistent
	private String value;

	@Persistent
	private Boolean valueBol;

	@Persistent
	private Double valueDouble;

	@Persistent
	private Long valueLong;

	@Persistent 
	private Date valueDate;

	// when did this start in time
	@Persistent
	private Date startOf;

	// when did this end in time
	@Persistent
	private Date endOf;

	// when this object was created
	@Persistent
	private Date dateCreated;

	// when this object was updated
	@Persistent
	private Date dateUpdated;

	// who created this object
	@Persistent
	private long createdByThingId;

	// who updated this object
	@Persistent
	private long updatedByThingId;

	/**
	 * constructor
	 */
	public ThingStuffJdo(ServerPersistence sp) {
		this.sp = sp;
	}

	public void setData(ThingStuffData thingStuffData) {
		if (thingStuffData == null) {
			return;
		}

		// can't do this here, bc it messes up the key gotten from getObjectById, cant mutate key
		//setKey(thingStuffData.getStuffId());

		this.thingId = thingStuffData.getThingId();
		this.thingStuffTypeId = thingStuffData.getThingStuffTypeId();

		this.value = thingStuffData.getValue();
		this.valueBol = thingStuffData.getValueBol();
		this.valueDouble = thingStuffData.getValueDouble();
		this.valueLong = thingStuffData.getValueLong();

		this.startOf = thingStuffData.getStartOf();
		this.endOf = thingStuffData.getEndOf();

		if (thingStuffData != null && thingStuffData.getStuffId() > 0) {
			this.dateUpdated = new Date();
		} else {
			this.dateCreated = new Date();
		}
	}

	public void setData(ThingStuffJdo thingStuffJdo) {
		if (thingStuffJdo == null) {
			return;
		}
		setKey(thingStuffJdo.getId());

		this.thingId = thingStuffJdo.getThingId();
		this.thingStuffTypeId = thingStuffJdo.getThingStuffTypeId();

		this.value = thingStuffJdo.getValue();
		this.valueBol = thingStuffJdo.getValueBol();
		this.valueDouble = thingStuffJdo.getValueDouble();
		this.valueLong = thingStuffJdo.getValueInt();

		this.startOf = thingStuffJdo.getStartOf();
		this.endOf = thingStuffJdo.getEndOf();

		if (thingStuffIdKey != null && thingStuffIdKey.getId() > 0) {
			this.dateUpdated = new Date();
		} else {
			this.dateCreated = new Date();
		}
	}

	private void setKey(long id) {
		if (id > 0) {
			thingStuffIdKey = getKey(id);
		}
	}

	public long save(ThingStuffData thingStuffData) {
		setData(thingStuffData);

		PersistenceManager pm = sp.getPersistenceManager();
		Transaction tx = pm.currentTransaction();
		try {
			tx.begin();

			if (thingStuffData != null && thingStuffData.getStuffId() > 0) { // update
				ThingStuffJdo update = pm.getObjectById(ThingStuffJdo.class, thingStuffData.getStuffId());
				update.setData(thingStuffData);
				this.thingStuffIdKey = update.thingStuffIdKey;

			} else { // insert
				thingStuffIdKey = null;
				pm.makePersistent(this);
			}

			tx.commit();

		} finally {
			if (tx.isActive()) {
				tx.rollback();
			}
			pm.close();
		}
		
		// debug
		System.out.println("ThingJdo: thingStuffId: " + getId() + " thingStuffTypeId: " + thingStuffTypeId + " " +
				"value: " + getString(value) + " valueBol: " + getString(valueBol) + " valueLong: " + getString(valueLong) + " valueDate: " + getString(valueDate));

		return getId();
	}

	/**
	 * query a thing by its id
	 * 
	 * @param thingStuffId
	 * @return
	 */
	public ThingStuffJdo query(long thingStuffId) {

		ThingStuffJdo thingStuff = null;
		PersistenceManager pm = sp.getPersistenceManager();
		try {
			thingStuff = pm.getObjectById(ThingStuffJdo.class, thingStuffId);
		} finally {
			pm.close();
		}

		return thingStuff;
	}

	public ThingStuffsData queryStuffs(ThingStuffFilterData filter) {

		ThingStuffData[] tsd = query(filter);

		ThingStuffsData tsds = new ThingStuffsData();
		tsds.total = 0; // TODO later
		tsds.thingStuffData = tsd;

		return tsds;
	}

	public ThingStuffData[] query(ThingStuffFilterData filter) {

		if (filter == null) {
			System.out.println("ERROR: ThingStuffJdo.query() Set a filter");
			return null;
		}

		long thingId = filter.thingId;

		ArrayList<ThingStuffJdo> aT = new ArrayList<ThingStuffJdo>();

		String qfilter = null;
		
		qfilter = "thingId==" + thingId + "";
		

		PersistenceManager pm = sp.getPersistenceManager();
		try {
			Extent<ThingStuffJdo> e = pm.getExtent(ThingStuffJdo.class, true);
			Query q = pm.newQuery(e, qfilter);
			q.execute();

			Collection<ThingStuffJdo> c = (Collection<ThingStuffJdo>) q.execute();
			Iterator<ThingStuffJdo> iter = c.iterator();
			while (iter.hasNext()) {
				ThingStuffJdo t = (ThingStuffJdo) iter.next();
				aT.add(t);
			}

			q.closeAll();
		} finally {
			pm.close();
		}

		ThingStuffJdo[] tj = new ThingStuffJdo[aT.size()];
		if (aT.size() > 0) {
			tj = new ThingStuffJdo[aT.size()];
			aT.toArray(tj);
		}

		// TODO overkill here - can get the list up above
		List<ThingStuffJdo> tjsa_list = Arrays.asList(tj);

		ThingStuffData[] td = convert(tjsa_list);

		return td;
	}
	
	public long queryTotal() {
		
		/* future spec I think
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		com.google.appengine.api.datastore.Query query = new com.google.appengine.api.datastore.Query("__Stat_Kind__");
		query.addFilter("kind_name", FilterOperator.EQUAL, TThingStuffAboutJdo.class);
		
    Entity globalStat = datastore.prepare(query).asSingleEntity();
    Long totalBytes = (Long) globalStat.getProperty("bytes");
    Long totalEntities = (Long) globalStat.getProperty("count");
		*/
		
		// TODO - work around, have to wait for the api/gae to make it to hosted mode
		long total = 0;
		
		PersistenceManager pm = sp.getPersistenceManager();
		try {
			Extent<ThingStuffJdo> e = pm.getExtent(ThingStuffJdo.class, true);
			Query q = pm.newQuery(e);
			q.execute();

			Collection<ThingStuffJdo> c = (Collection<ThingStuffJdo>) q.execute();
			total = c.size();

			q.closeAll();
		} finally {
			pm.close();
		}
		
		return total;
	}

	public static ThingStuffData[] convert(List<ThingStuffJdo> thingStuffJdoAbout) {

		Iterator<ThingStuffJdo> itr = thingStuffJdoAbout.iterator();

		ThingStuffData[] r = new ThingStuffData[thingStuffJdoAbout.size()];

		int i = 0;
		while(itr.hasNext()) {

			ThingStuffJdo tsja = itr.next();

			r[i] = new ThingStuffData();
			r[i].setData(
					tsja.getThingId(),
					tsja.getStuffId(), 
					tsja.getStuffTypeId(), 

					tsja.getValue(), 
					tsja.getValueBol(), 
					tsja.getValueDouble(),
					tsja.getValueInt(), 
					tsja.getStartOf(),
					tsja.getEndOf(), 

					tsja.getDateCreated(),
					tsja.getDateUpdated());

			i++;
		}

		return r;
	}

	public List<ThingStuffJdo> convertStuffsAboutToJdo(ThingStuffsData thingStuffsData) {

		if (thingStuffsData == null) {
			return null;
		}

		ThingStuffData[] tsd = thingStuffsData.getThingStuffData();

		ThingStuffJdo[] r = new ThingStuffJdo[tsd.length];

		for (int i=0; i < tsd.length; i++) {
			r[i] = new ThingStuffJdo(sp);
			r[i].thingId = tsd[i].getThingId();
			r[i].thingStuffIdKey = getKey(tsd[i].getStuffId());
			r[i].thingStuffTypeId = tsd[i].getThingStuffTypeId();

			r[i].value = tsd[i].getValue();
			r[i].valueBol = tsd[i].getValueBol();
			r[i].valueDouble = tsd[i].getValueDouble();
			r[i].valueLong = tsd[i].getValueLong();

			r[i].startOf = tsd[i].getStartOf();
			r[i].endOf = tsd[i].getEndOf();
		}

		List<ThingStuffJdo> l = Arrays.asList(r);

		return l;
	}

	public boolean delete(long thingStuffTypeId) {

		System.out.println("ThingStuffJdo.delete(): deleting: " + thingStuffTypeId);

		PersistenceManager pm = sp.getPersistenceManager();
		Transaction tx = pm.currentTransaction();
		boolean b = false;
		try {
			tx.begin();

			ThingStuffJdo ttj2 = (ThingStuffJdo) pm.getObjectById(ThingStuffJdo.class, thingStuffTypeId);
			pm.deletePersistent(ttj2);

			tx.commit();
			b = true;
		} catch (Exception e) {
			e.printStackTrace();
			b = false;
		} finally {
			if (tx.isActive()) {
				tx.rollback();
				b = false;
			}
			pm.close();
		}

		return b;
	}

	public boolean delete(ThingData thingData) {

		if (thingData == null) {
			return false;
		}

		long thingId = thingData.getThingId();

		if (thingId == 0) {
			return false;
		}

		String qfilter = "thingId==" + thingId + "";

		PersistenceManager pm = sp.getPersistenceManager();
		Transaction tx = pm.currentTransaction();
		try {
			tx.begin();

			Extent<ThingStuffJdo> e = pm.getExtent(ThingStuffJdo.class, true);
			Query q = pm.newQuery(e, qfilter);
			q.execute();

			Collection<ThingStuffJdo> c = (Collection<ThingStuffJdo>) q.execute();

			// delete all
			pm.deletePersistentAll(c);

			tx.commit();
			q.closeAll();
		} finally {
			if (tx.isActive()) {
				tx.rollback();
			}
			pm.close();
		}

		return true;
	}

	/**
	 * get key long identity number
	 * @return
	 */
	public long getId() {
		if (thingStuffIdKey == null) {
			return -1;
		}
		return thingStuffIdKey.getId();
	}

	/**
	 * get id (same as getId()
	 * @return
	 */
	public long getStuffId() {
		return thingStuffIdKey.getId();
	}

	public long getStuffTypeId() {
		return thingStuffTypeId;
	}

	public long getThingId() {
		return thingId;
	}

	public long getThingStuffTypeId() {
		return thingStuffTypeId;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public void setValue(Boolean value) {
		this.valueBol = value;
	}

	public void setValue(Double value) {
		this.valueDouble = value;
	}

	public String getValue() {
		return value;
	}

	public Boolean getValueBol() {
		return valueBol;
	}

	public Double getValueDouble() {
		return valueDouble;
	}

	public Long getValueInt() {
		return valueLong;
	}

	public Date getStartOf() {
		return startOf;
	}

	public Date getEndOf() {
		return endOf;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public Date getDateUpdated() {
		return dateUpdated;
	}

	public long getCreatedBy() {
		return createdByThingId;
	}

	public long getUpdatedBy() {
		return updatedByThingId;
	}

	private Key getKey(long id) {
		Key key = null;
		if (id == 0) {
			key = KeyFactory.createKey(ThingStuffJdo.class.getSimpleName(), id);
		}
		return key;
	}



	private String getString(Boolean value) {
		String s = "";
		if (value != null) {
			s = value.toString();
		}
		return s;
	}

	private String getString(Date value) {
		String s = "";
		if (value != null) {
			s = value.toString();
		}
		return s;
	}

	private String getString(Long value) {
		String s = "";
		if (value != null) {
			s = Long.toString(value);
		}
		return s;
	}

	private String getString(String value) {
		String s = "";
		if (value != null) {
			s = value;
		}
		return s;
	}

}

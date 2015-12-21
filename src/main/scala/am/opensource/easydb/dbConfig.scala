package am.opensource.easydb

import net.liftweb.common.{Loggable, Empty, Box, Full}
import net.liftweb.util.Props
import net.liftweb.util.ControlHelpers.tryo

/**
  * Created by silvs on 21/12/2015.
  */
object dbConfig extends Loggable {

	case class configOption[T](systemProp:String = "", liftProp:String = "", jndi:String = "", failover: Box[T] = Empty){

		def getSystemProp:Box[T] = {
			logger.trace("Trying SystemProp: "+systemProp)
			Box.legacyNullTest(System.getProperty(systemProp)) flatMap (x => tryo(x.asInstanceOf[T])) match {
				case x if x.isDefined =>
					logger.trace("Success for SystemProp: "+systemProp+" - "+ x.toString)
					x
				case _ => Empty
			}
		}
		def getLiftProp:Box[T] = {
			logger.trace("Trying LiftProp: "+liftProp)
			Props.get(liftProp) flatMap (x => tryo(x.asInstanceOf[T])) match {
				case x if x.isDefined =>
					logger.trace("Success for LiftProp: "+liftProp+" - "+ x.toString)
					x
				case _ => Empty
			}
		}
		def getJNDIProp:Box[T] = {
			logger.trace("Trying jndi: "+jndi)
			JNDILookup[T](jndi) match {
				case x if x.isDefined =>
					logger.trace("Success for jndi: "+jndi+" - "+ x.toString)
					x
				case _ => Empty
			}
		}

		def apply():T = getSystemProp or getLiftProp or getJNDIProp or failover openOrThrowException "No valid config prop's!"
	}

	var DataSourceClassName =
		configOption[String](
			systemProp = "RDS_CLASSNAME",
			liftProp = "db.classname",
			jndi = "jdbc/db/classname"
		)

	var DataSourceHost =
		configOption[String](
			systemProp = "RDS_HOSTNAME",
			liftProp = "db.hostname",
			jndi = "jdbc/db/host"
		)

	var DataSourcePort =
		configOption[Integer](
			systemProp = "RDS_PORT",
			liftProp = "db.port",
			jndi = "jdbc/db/port"
		)

	var DataSourceDatabase =
		configOption[String](
			systemProp = "RDS_DB_NAME",
			liftProp = "db.db_name",
			jndi = "jdbc/db/database"
		)

	var DataSourceUser =
		configOption[String](
			systemProp = "RDS_USERNAME",
			liftProp = "db.username",
			jndi = "jdbc/db/user"
		)

	var DataSourcePassword =
		configOption[String](
			systemProp = "RDS_PASSWORD",
			liftProp = "db.password",
			jndi = "jdbc/db/password"
		)


	var PoolMinimumIdle =
		configOption[Integer](
			systemProp = "DBPOOL_MINIDLE",
			liftProp = "db.pool.minidle",
			jndi = "jdbc/pool/minidle",
			failover = Full(4)
		)
	var PoolMaximumPoolSize =
		configOption[Integer](
			systemProp = "DBPOOL_MAXSIZE",
			liftProp = "db.pool.maxsize",
			jndi = "jdbc/pool/maxsize",
			failover = Full(32)
		)
	var PoolConnectionTimeout =
		configOption[Long](
			systemProp = "DBPOOL_CONNTIMEOUT",
			liftProp = "db.pool.conntimeout",
			jndi = "jdbc/pool/conntimeout",
			failover = Full(300000)
		)
	var PoolIdleTimeout =
		configOption[Long](
			systemProp = "DBPOOL_IDLETIMEOUT",
			liftProp = "db.pool.idletimeout",
			jndi = "jdbc/pool/idletimeout",
			failover = Full(60000)
		)
	var PoolLeakDetectionThreshold =
		configOption[Long](
			systemProp = "DBPOOL_LEAKTHRESHOLD",
			liftProp = "db.pool.leakthreshold",
			jndi = "jdbc/pool/leakthreshold",
			failover = Full(120000)
		)
	var PoolMaxLifetime =
		configOption[Long](
			systemProp = "DBPOOL_MAXLIFETIME",
			liftProp = "db.pool.maxlifetime",
			jndi = "jdbc/pool/maxlifetime",
			failover = Full(3600000)
		)
}

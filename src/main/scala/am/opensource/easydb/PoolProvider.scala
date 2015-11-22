package am.opensource.easydb

import java.io.Closeable
import java.sql.Connection

import com.zaxxer.hikari._
import net.liftweb.common.{Box, Empty, Full, Loggable}
import net.liftweb.mapper._
import net.liftweb.util.Helpers

object PoolProvider extends ConnectionManager with Closeable with Loggable {
	private[this] lazy val pool = {
		val config = new HikariConfig()
		config.setDataSourceClassName(JNDILookup[String]("jdbc/db/classname"))
		config.addDataSourceProperty("host",JNDILookup[String]("jdbc/db/host"))
		config.addDataSourceProperty("port",JNDILookup[Integer]("jdbc/db/port"))
		config.addDataSourceProperty("database",JNDILookup[String]("jdbc/db/database"))
		config.addDataSourceProperty("user", JNDILookup[String]("jdbc/db/user"))
		config.addDataSourceProperty("password", JNDILookup[String]("jdbc/db/password"))

		config.setMinimumIdle(JNDILookup[Integer]("jdbc/pool/minidle"))
		config.setMaximumPoolSize(JNDILookup[Integer]("jdbc/pool/maxsize"))
		config.setConnectionTimeout(JNDILookup[Long]("jdbc/pool/conntimeout"))
		config.setIdleTimeout(JNDILookup[Long]("jdbc/pool/idletimeout"))
		config.setLeakDetectionThreshold(JNDILookup[Long]("jdbc/pool/leakthreshold"))
		config.setMaxLifetime(JNDILookup[Long]("jdbc/pool/maxlifetime"))

		new HikariDataSource(config)
	}

	def newConnection(name: ConnectionIdentifier): Box[Connection] = {
		try {
			Full(getConnection)
		} catch {
			case e : Exception => e.printStackTrace(); Empty
		}
	}
	def getConnection = pool.getConnection
	def releaseConnection(conn: Connection) = conn.close()
	def shutdown() = pool.shutdown()
	def close() = shutdown()

	override def newSuperConnection(name:ConnectionIdentifier): Box[SuperConnection] = {
		newConnection(name).map(c => {
			val uniqueId = if (logger.isDebugEnabled) Helpers.nextNum.toString else ""
			logger.debug("Connection ID " + uniqueId + " for JNDI connection " + name.jndiName + " opened")
			new SuperConnection(c, () => {logger.debug("Connection ID " + uniqueId + " for JNDI connection " + name.jndiName + " closed"); c.close})
		})
	}

}


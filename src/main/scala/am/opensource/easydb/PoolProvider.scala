package am.opensource.easydb

import java.io.Closeable
import java.sql.Connection

import com.zaxxer.hikari._
import net.liftweb.common.{Box, Empty, Full, Loggable}
import net.liftweb.mapper._
import net.liftweb.util.{Props, Helpers}

object PoolProvider extends ConnectionManager with Closeable with Loggable {



	private[this] lazy val pool = {
		val config = new HikariConfig()
		config.setDataSourceClassName(dbConfig.DataSourceClassName())
		config.addDataSourceProperty("host",dbConfig.DataSourceHost())
		config.addDataSourceProperty("port",dbConfig.DataSourcePort())
		config.addDataSourceProperty("database",dbConfig.DataSourceDatabase())
		config.addDataSourceProperty("user", dbConfig.DataSourceUser())
		config.addDataSourceProperty("password", dbConfig.DataSourcePassword())

		config.setMinimumIdle(dbConfig.PoolMinimumIdle())
		config.setMaximumPoolSize(dbConfig.PoolMaximumPoolSize())
		config.setConnectionTimeout(dbConfig.PoolConnectionTimeout())
		config.setIdleTimeout(dbConfig.PoolIdleTimeout())
		config.setLeakDetectionThreshold(dbConfig.PoolLeakDetectionThreshold())
		config.setMaxLifetime(dbConfig.PoolMaxLifetime())

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


package am.opensource.easydb


import net.liftweb.common.Loggable
import net.liftweb.db.DBLogEntry
import net.liftweb.mapper.{BaseMetaMapper, DB, Schemifier}
import net.liftweb.util.Props

/**
  * Created by silvs on 19/11/2015.
  */
object Database extends Loggable {

	def schemeLogger(msg: => AnyRef) = {
		logger.info(msg)
	}

	// Setup database connection pool
	def connect() = DB.defineConnectionManager(net.liftweb.util.DefaultConnectionIdentifier, PoolProvider)

	def shutdown() = PoolProvider.shutdown()

	def schemify(stables: BaseMetaMapper*) = try {
		Schemifier.schemify(
			true, // Write schema to db
			msg => schemeLogger(msg), // Log function
			stables :_*
		)
	} catch {
		case e: Exception =>
			if (Props.mode == Props.RunModes.Development) {
				e.printStackTrace()
				logger.error("Unable to obtain database connection in Boot.")
				sys.exit()
			}
			else
				throw e
	}

	def addLogger(showStack: () => Boolean = () => true, stackFilter: StackTraceElement => Boolean = p => true) = {
		if (Props.mode == Props.RunModes.Development) {
			DB.addLogFunc {
				case (log, duration) =>
					logger.debug("Total query time : %d ms".format(duration))
					log.allEntries.filter(_.statement.startsWith("Exec")).foreach {
						case DBLogEntry(stmt, duration2) =>
							if (showStack()) logger.trace("%s in %d ms \n%s"
								.format(
									stmt,
									duration2,
									Thread.currentThread().getStackTrace.filter(stackFilter).mkString("DBD: ", "\nDBD: ", "\n")
								)
							)
					}
			}
		}
		DB.addLogFunc(DB.queryCollector)
	}
}

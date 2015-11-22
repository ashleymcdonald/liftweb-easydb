package am.opensource.easydb


import net.liftweb.common.Loggable
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


	def schemify(stables: BaseMetaMapper*) = try {
		Schemifier.schemify(
			true, // Write schema to db
			schemeLogger _, // Log function
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
}

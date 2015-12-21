package am.opensource.easydb

import javax.naming.{Context, InitialContext}

import net.liftweb.common.{Box, Empty, Loggable}
import net.liftweb.util.Helpers._

import scala.collection.mutable.ListBuffer

//TEST

object JNDILookup extends Loggable {
	private[this] val paths: List[String] = List("java:comp/env","java:comp/env/jdbc","java:comp/env/ejb","java:comp/UserTransaction","java:comp/env/mail","java:comp/env/url","java:comp/env/jms","java:comp/ORB")
	private[this] val toTry: ListBuffer[(String)=>Any] = ListBuffer()
	private[this] def firstTime() {
		paths.map(
			path=> {
				toTry += ((name) => {
					logger.trace("Trying JNDI lookup on "+path+" followed by lookup on %s".format(name))
					(new InitialContext).lookup(path).asInstanceOf[Context].lookup(name)
				})
				toTry += ((name) => {
					logger.trace("Trying JNDI lookup on "+path+"/%s".format(name))
					(new InitialContext).lookup(path + "/" + name)
				})
			}
		)
		toTry += ((name) => {
			logger.trace("Trying JNDI lookup on %s".format(name))
			(new InitialContext).lookup(name)
		})
	}
	private[this] def jndiLookup[T](name: String): Box[T] = {
		if(toTry.isEmpty) {
			firstTime()
		}
		first(toTry) (f => tryo{t:Throwable => logger.trace("JNDI Lookup failed: "+t)}(f(name).asInstanceOf[T])) or {
			logger.trace("Unable to obtain data for JNDI name %s".format(name))
			Empty
		}
	}
	def apply[T](name: String): Box[T] = {
		jndiLookup[T](name) //.openOrThrowException("Missing key "+name+" from JNDI lookup? BARF")
	}
}

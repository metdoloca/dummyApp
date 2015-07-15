package scriptable

case class Command(fileName:String, code:String)
case class LogLine(line:String,lineNumber:Int)
case class RunCommand(fileName:String, code:String, sessionKey:String)
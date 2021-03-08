require 'logger'

$logger = Logger.new(STDOUT,formatter: proc {|severity, datetime, prog_name, msg|
  "#{severity[0]}::#{msg}\n"
})
# severity == "DEBUG"
$logger.level = :debug
def eval_log(msg)
  $logger.debug "Eval::#{msg}"
end
# $logger.debug 'debug'
# $logger.info 'info'
# $logger.warn 'warn'
# $logger.error 'error'
# $logger.fatal 'fatal'
require 'logger'

$logger = Logger.new(STDOUT, formatter: proc { |severity, datetime, prog_name, msg|
  if severity == "ERROR"
    "\033[31m#{severity[0]}::#{msg}\n"
  else
    "\033[371m#{severity[0]}::#{msg}\n"
  end
})

# severity == "DEBUG"
$logger.level = :error
# %w[debug info warn error fatal]
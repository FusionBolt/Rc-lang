require './Lib/log'

class Env < Hash
  def initialize(*several_variants)
    super
    update init
  end

  def enter_fun(args_env)
    @backup = self.dup
    self.update args_env
  end

  def exit_fun
    self.clear
    self.update @backup
  end

  def init
    init_debug_info.merge(init_exception).merge(init_args([]))
  end

  # TODO:when replace with other text
  # will changed every one which used it
  # changed it by other way
  def init_debug_info
    {'!!SaveMainEnv' => true}
  end
  
  def init_exception
    {'!!ExceptionInfo' => []}
  end

  def init_args(argv = [])
    {'!!ARGC' => argv.length, '!!ARGV' => argv}
  end

  def define_symbol(symbol_name, define = nil)
    if has_key? symbol_name
      raise 'SymbolReDefine'
    else
      eval_log "symbol:#{symbol_name} define:#{define} has been added to env"
      super(symbol_name, define)
    end
  end

  def find_symbol(symbol_name)
    if has_key? symbol_name
      super symbol_name
    else
      raise 'NoSymbolDefine'
    end
  end

  def update_symbol(symbol_name, define)
    if has_key? symbol_name
      self[symbol_name] = define
    else
      raise 'SymbolNotFound'
    end
  end
end
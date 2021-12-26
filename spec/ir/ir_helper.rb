require './analysis/global_env'
require './ir/tac/tac'

module RcTestHelpers
  def get_tac(src)
    ast = parse(src)
    env, sym_table = Rc::Analysis::GlobalEnvVisitor.new.analysis(ast)
    Rc::TAC.to_tac(ast, env)
  end

  def get_first_fun_tac_list(src)
    get_tac(src).first_fun_tac_list
  end
end
require './analysis/global_env'
require './ir/tac/tac'

module RcTestHelpers
  def get_tac(src)
    ast = parse(src)
    env = Rc::Analysis::GlobalEnvVisitor.new.analysis(ast)
    Rc::TAC.to_tac(ast, env.define_env)
  end

  def get_first_fun_tac_list(src)
    get_tac(src).first_fun_tac_list
  end
end
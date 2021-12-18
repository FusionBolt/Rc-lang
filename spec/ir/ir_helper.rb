require_relative '../../analysis/global_env'
require_relative '../../ir/tac/tac'

module RcTestHelpers
  def get_tac(src)
    ast = parse(src)
    env, sym_table = Rc::Analysis::GlobalEnvVisitor.new.analysis(ast)
    Rc::TAC.to_tac(ast, env)
  end
end
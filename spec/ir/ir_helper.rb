require_relative '../../analysis/global_env'
require_relative '../../ir/tac/tac'

module RcTestHelpers
  def get_tac(src)
    ast = parse(src)
    env = Rc::Analysis::GlobalEnvVisitor.new.analysis(ast)
    Rc::TAC.to_tac(ast, env)
  end
end
require './ir/ast/visitor'
require './lib/env'
require_relative './inst'
require_relative './translator'

module Rc::VM

  def to_vm_inst(ast, global_env)
    VMInstTranslator.new.translate(ast, global_env)
  end


  module_function :to_vm_inst
end

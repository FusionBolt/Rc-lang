require_relative 'helper'

module Rc
  class Evaluate
    attr_accessor :env

    def initialize(visitor, env = {})
      @visitor, @env = visitor, env
    end

    # TODO:log indent
    # error process and refactor
    def evaluate(node)
      method("eval_#{Helper::under_score_class_name(node)}")[node]
    end

    def env_evaluate(node, env)
      @env.subroutine(env) do
        evaluate(node)
      end
    end

    def eval_expression(node)
      # TODO:changed
      # TODO:list order??
      term_list = node.term_list
      if term_list.length == 1
        evaluate(term_list[0])
      else
        eval_str = term_list.map { |n| evaluate(n) }.join(' ')
        Kernel.eval(eval_str)
      end
    end

    def eval_fun_call(node)
      name = node.name
      fun = @env[name]
      if fun.class == Rc::Function
        eval_log "call:#{name}"
        # TODO:param check
        return_val = @visitor.run_fun(fun, node.args)
        eval_log "call:#{name} end"
        return_val
      else
        raise "call #{name} failed, error env:: #{@env}"
      end
    end

    def eval_class_member_access(node)
      # TODO:member fun access args
      member_name = node.member_name
      node = @env[node.instance_name]
      if node.class_define.fun_env.include? member_name
        args = []
        @visitor.run_fun(node.class_define.fun_env[member_name], args)
      elsif node.instance_env.include? member_name
        evaluate(node.instance_env[member_name])
      else
        raise 'NoMember', member_name
      end
    end

    def eval_identifier(node)
      @env[node.name]
    end

    def eval_instance(node)
      if node.is_obj
        raise 'UnfinishedException'
      else
        evaluate(node.instance_env[:_val])
      end
    end

    def eval_new_expr(node)
      class_node = @env[node.class_name]
      # TODO:replace
      @visitor.run_fun(class_node.instance_constructor, node.args)
      Instance.new(class_node, class_node.var_env, true)
    end

    def eval_op(node)
      node.op
    end

    def eval_bool_constant(node)
      node.val
    end

    def eval_number_constant(node)
      Kernel.eval(node.val)
    end

    def eval_string_constant(node)
      Kernel.eval(node.val)
    end
  end
end
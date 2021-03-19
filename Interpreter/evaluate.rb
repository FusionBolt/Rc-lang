require_relative 'helper'
require './Lib/Rc/assert'
require './Lib/error'

module Rc
  class Evaluate
    # ensure lib fun can be found by method
    include Rc::Lib
    attr_accessor :env

    def initialize(visitor, env = {})
      @visitor, @env = visitor, env
    end

    def evaluate(node)
      # TODO:error process, in this or visit expr?
      method("eval_#{Helper::under_score_class_name(node)}")[node]
    end

    def env_evaluate(node, env)
      @env.sub_scope(env) do
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

    def eval_lambda(node)
      node
    end

    def eval_fun_call(node)
      name = node.name
      fun = @env[name] rescue nil
      # normal function or lambda
      if fun.is_a? Rc::Function
        # TODO:param check
        # TODO:args need eval?
        @visitor.run_fun(fun, node.args)
      else
        begin
        method(name.to_sym).call(*node.args.map{|arg| evaluate(arg)})
        rescue NameError => e
          raise SymbolNotFoundError.new(name)
        end
      end
    end

    def eval_class_member_access(access)
      member_name = access.member_name
      instance = @env[access.instance_name]
      send_msg(access.instance_name, instance, member_name, access.args)
    end

    def send_msg(instance_name, instance, symbol, args)
      var = instance.fetch_var(symbol)
      unless var.nil?
        return evaluate(var)
      end
      fun = instance.fetch_fun(symbol)
      unless fun.nil?
        return @visitor.run_fun(fun, args)
      end
      raise ClassMemberNotFound.new(instance_name, instance, symbol)
    end

    def eval_identifier(node)
      @env[node.name]
    end

    def eval_instance(node)
      if node.is_obj
        raise UnFinishedError.new(node)
      else
        evaluate(node.instance_env[:_val])
      end
    end

    def eval_new_expr(node)
      class_node = @env[node.class_name]
      # TODO:replace
      @visitor.run_fun(class_node.instance_constructor, node.args)
      Instance.new(class_node, class_node.instance_var_env, true)
    end

    def eval_op(node)
      node.op
    end

    def eval_bool_constant(node)
      Kernel.eval(node.val)
    end

    def eval_number_constant(node)
      Kernel.eval(node.val)
    end

    def eval_string_constant(node)
      Kernel.eval(node.val)
    end
  end
end
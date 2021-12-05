require_relative '../lib/helper'
require './lib/Rc/assert'
require './lib/error'
require './lib/ffi'

module Rc
  class Evaluator
    attr_accessor :env

    def initialize(visitor, call_stack, env = {})
      @visitor, @call_stack, @env = visitor, call_stack, env
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

    def eval_expr(node)
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
      if @env.has_symbol? name
        fun = @env[name]
        if fun.is_a? Rc::Function
          run_fun(fun, node.args)
        else
          raise SymbolNotFoundError.new(name)
        end
      else
        FFI.call(name, node.args, self)
      end
    end

    def run_fun(fun, args, obj_instance = nil)
      if args.length != fun.args.length
        raise Rc::ArgsLengthNotMatchError.new(fun.name, args.length, fun.args.length)
      end
      args_env = fun.args.zip(args.map { |arg| evaluate(arg) }).to_h
      @env.sub_scope(args_env) do
        @call_stack.subroutine(StackFrame.new(@call_stack.cur_stmt, fun, @env, obj_instance)) do
          rtn_val = @visitor.visit(fun.stmts)
          # TODO:refactor
          if fun.name == 'main'
            $logger.debug @env.inspect
          end
          rtn_val
        end
      end
    end

    def eval_class_member_access(access)
      member_name = access.member_name
      instance = nil
      if access.instance_name == 'self'
        instance = @call_stack.cur_obj
      else
        instance = @env[access.instance_name]
      end
      send_msg(access.instance_name, instance, member_name, access.args)
    end

    def send_msg(instance_name, instance, symbol, args)
      var = instance.fetch_var(symbol)
      unless var.nil?
        if var.class == Expr
          return evaluate(var)
        else
          return var
        end
      end
      fun = instance.fetch_fun(symbol)
      unless fun.nil?
        return run_fun(fun, args, instance)
      end
      raise ClassMemberNotFound.new(instance_name, instance, symbol)
    end

    def eval_identifier(node)
      @env[node.name]
    end

    def eval_instance(node)
      if node.is_obj
        # raise UnFinishedError.new(node)
        # TODO: when call ruby fun, eval will error
        node
      else
        evaluate(node.instance_env[:_val])
      end
    end

    def eval_new_expr(node)
      class_node = @env[node.class_name]
      i = Instance.new(class_node, class_node.instance_var_env, true)
      run_fun(class_node.instance_constructor, node.args, i)
      i
    end

    def eval_op(node)
      node.op
    end

    def eval_constant(node)
      Kernel.eval(node.val)
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
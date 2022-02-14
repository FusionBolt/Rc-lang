require './ir/ast/visitor'
require './lib/env'
require_relative './inst'
require_relative './translator'
require './lib/generate'

module Rc
  module VM
    def gen_class_table(global_env)
      # todo:instance_vars should not save var value
      global_env.class_table.map do |class_name, table|
        <<SRC
#{class_name} #{table.parents.generate(' ', &:to_s)}
#{table.instance_vars.keys.generate(' ', &:to_s)}
#{table.instance_methods.map { |name, info| gen_method(name, info) }.join("\n") }
SRC
      end.join("\n")
    end

    def gen_method(name, method_info)
      <<SRC
#{name} #{method_info.args.size} #{method_info.env.size}
#{method_info.define.map(&:to_s).join("\n")}
SRC
    end

    def generate(root, name, content)
      File.open(File.join(root, name), 'w') do |f|
        f.write(content)
      end
    end

    def to_vm_inst(global_env)
      VMInstTranslator.new.translate(global_env)
    end

    def generate_all(global_table, root)
      generate(root, 'class_table.rckls', gen_class_table(global_table))
    end

    module_function :to_vm_inst, :generate_all, :generate, :gen_method, :gen_class_table
  end
end

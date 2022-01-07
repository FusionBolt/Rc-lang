require_relative 'inst'
require './lib/type_struct'
require './lib/helper'

class Array
  def generate(c = "\n", &f)
    map {|a| f[a] }.join(c)
  end

  def pure_generate(&f)
    map { |a| a.demodulize_class }.generate(&f)
  end
end

def gen_enum_inst_type(classes)
  <<SRC
enum class InstType {
#{classes.pure_generate {|c| "#{c},"}}
};
SRC
end

def gen_inst_base_class
  <<SRC
struct VMInst
{
  InstType _type;
protected:
  VMInst(InstType type): _type(type) {};
};
SRC
end

def gen_class_member(type_define, prefix='')
  "#{TypeCheck::VALID_TYPE[type_define.type]} #{prefix}#{type_define.name}"
end

def gen_class_define(klass)
  class_name = klass.demodulize_class
  member_map = klass.get_member_map
  params = member_map.generate(', ') {|td| gen_class_member(td)}
  init_member = "#{member_map.keys.generate(', ') {|name| "_#{name}(#{name})"}}"
  init_member = ", #{init_member}" unless init_member.empty?
  init_inst = "VMInst(InstType::#{class_name})"
  <<SRC
struct #{class_name} : VMInst
{
public:
  #{class_name}(#{params}):#{init_inst}#{init_member} {}

private:
#{member_map.generate {|mem_ty| "#{gen_class_member(mem_ty, '_')};"}}
};
SRC
end

def gen_classes_define(classes)
  # todo:direct pass fun
  gen_inst_base_class + classes.generate { |x| gen_class_define(x) }
end

def type_translate(td, index)
  str = "list[#{index}]"
  if td.type == :int
    "std::stoi(#{str})"
  else
    str
  end
end

def gen_parser(klass)
  class_name = klass.demodulize_class
  member_map = klass.get_member_map
  index = 1
  args = "#{member_map.generate(', ') {|td| type_translate(td, index).tap { index += 1 }} }"
  <<SRC
if (list[0] == "#{class_name}") return std::make_unique<#{class_name}>(#{args});
SRC
end

def gen_all_parser(classes)
  <<SRC
std::unique_ptr<VMInst> get_inst(const std::vector<std::string> &list)
{
#{classes.generate {|x| gen_parser(x)}}
}
SRC
end

# todo:split multi module and mixin
def get_classes(mod)
  mod.constants.map{|c| mod.const_get(c)}.select{|c| c.is_a? Class}.sort_by{ |klass| klass.to_s }
end

# todo:limit only in this file
class Symbol
  def pure_name
    to_s[..-3]
  end
end

class TypeDefine < Struct.new(:name, :type)
end

class MemberMap
  def initialize(type_defines)
    @type_defines = type_defines
  end

  def generate(c = "\n", &f)
    @type_defines.generate(c, &f)
  end

  def keys
    @type_defines.map { |td| td.name }
  end
end

class Class
  def get_member_map
    instance = self.new
    # need keep same order
    MemberMap.new(instance.methods.filter {|m| m.to_s.end_with? '_t' }.map do |m|
      TypeDefine.new(m.pure_name, instance.send(m.to_sym))
    end)
  end
end

def gen_header_namespace
  <<SRC
#include <string>
#pragma once

using std::string;
SRC
end

classes = get_classes(Rc::VM::Inst)
# puts gen_enum_inst_type(classes)
# puts gen_classes_define(classes)
# puts gen_all_parser(classes)

cpp_src = gen_header_namespace + gen_enum_inst_type(classes) + gen_classes_define(classes) + gen_all_parser(classes)
File.open('/home/homura/Code/RCVM/instructions.hpp', 'w').write(cpp_src)
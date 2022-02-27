require_relative 'inst'
require './lib/type_struct'
require './lib/helper'
require './lib/generate'

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
  InstType type;
  virtual std::string to_string() const { return "VMInst"; }
protected:
  VMInst(InstType t): type(t) {};
  virtual ~VMInst() = default;
};
SRC
end

def gen_class_member(type_define, prefix='')
  "#{TypeCheck::VALID_TYPE[type_define.type]} #{prefix}#{type_define.name}"
end

def gen_class_define(klass)
  class_name = klass.demodulize_class
  member_map = klass.get_member_map
  params = member_map.generate(', ') {|td| gen_class_member(td, '_')}
  init_member = "#{member_map.keys.generate(', ') {|name| "#{name}(_#{name})"}}"
  init_member = ", #{init_member}" unless init_member.empty?
  init_inst = "VMInst(InstType::#{class_name})"
  to_string = "\"#{class_name}:\""
  if member_map.keys.size != 0
    to_string = to_string + "+" + (member_map.generate(" +"){|td| td.type == :int ? "std::to_string(#{td.name})" : td.name})
  end
  <<SRC
struct #{class_name} : VMInst
{
public:
  #{class_name}(#{params}):#{init_inst}#{init_member} {}

  std::string to_string() const override 
  { 
    return #{to_string};
  }

#{member_map.generate {|mem_ty| "#{gen_class_member(mem_ty)};"}}
};
SRC
end


def gen_classes_define(classes)
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
if (list[0] == "#{class_name}") return std::make_shared<#{class_name}>(#{args});
SRC
end

def gen_all_parser(classes)
  <<SRC
inline std::shared_ptr<VMInst> get_inst(const std::vector<std::string> &list)
{
#{classes.generate {|x| gen_parser(x)}}
throw std::runtime_error("Unknown inst type" + list[0]);
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
    # need keep same order
    MemberMap.new(self.try(:type_map).or_else{[]}.map do |name, type|
      TypeDefine.new(name, type)
    end)
  end
end

def gen_header_namespace
  <<SRC
#include <string>
#include <vector>
#include <memory>
#include <iostream>
#pragma once

using std::string;
SRC
end


def gen_visit(klass)
  <<SRC
    case InstType::#{klass}:
      visit(static_cast<const #{klass}&>(inst));break;
SRC
end

def gen_visitor
  classes = get_classes(Rc::VM::Inst)
  <<SRC
#include "rcvm.h"

using namespace RCVM;
void VMInstVisitor::accept(const VMInst &inst)
{
    switch(inst.type)
    {
#{classes.pure_generate{|x| gen_visit(x)}}
      default:
        throw std::runtime_error("not supported inst" + inst.to_string());
    }
}
SRC
end

def gen_inst_src
  classes = get_classes(Rc::VM::Inst)
  gen_header_namespace + gen_enum_inst_type(classes) + gen_classes_define(classes) + gen_all_parser(classes)
end
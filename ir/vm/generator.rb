require_relative 'inst'
require './lib/helper'

class Array
  def generate(&f)
    map {|a| f[a] }.join("\n")
  end

  def pure_generate(&f)
    map { |a| a.demodulize_class }.generate(&f)
  end
end

def gen_enum_inst_type(classes)
  <<SRC
enum class InstType {
#{classes.pure_generate {|c| "#{c},"}}
}
SRC
end

def gen_inst_base_class
  <<SRC
struct VMInst
{
  InstType type;
protected:
  VMInst() = default;
}
SRC
end

def gen_class_define(klass)
  class_name = klass.demodulize
  <<SRC
struct #{class_name} : VMInst
{
  #{class_name}()
  {
    type = InstType::#{class_name}
  }
}
SRC
end

def gen_classes_define(classes)
  # todo:direct pass fun
  gen_inst_base_class + classes.generate { |x| gen_class_define(x) }
end

def gen_parser(klass)
  class_name = klass.demodulize
  <<SRC
if (list[0] == "#{class_name}") return std::make_unique<#{class_name}>();
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

def get_classes(mod)
  mod.constants.map{|c| mod.const_get(c)}.select{|c| c.is_a? Class}
end

classes = get_classes(Rc::VM::Inst)
puts gen_enum_inst_type(classes)
puts gen_classes_define(classes)
puts gen_all_parser(classes)
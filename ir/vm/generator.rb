require_relative 'inst'

class Array
  def generate(&f)
    map {|a| f[a] }.join("\n")
  end
end

def gen_enum_inst_type(classes)
  <<SRC
enum class InstType {
#{classes.generate {|c| "#{c},"}}
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
  <<SRC
struct #{klass} : VMInst
{
  #{klass}()
  {
    type = InstType::#{klass}
  }
}
SRC
end

def gen_classes_define(classes)
  # todo:direct pass fun
  gen_inst_base_class + classes.generate { |x| gen_class_define(x) }
end

def gen_parser(klass)
  <<SRC
if (list[0] == "#{klass}") return std::make_unique<#{klass}>();
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
  mod.constants.select {|c| mod.const_get(c).is_a? Class}
end
klass = Object.const_get("Rc::VM::Inst::Add")
def get_classes_info(mod, classes)
  classes.map {|x| }
end
classes = get_classes(Rc::VM::Inst)
puts gen_enum_inst_type(classes)
puts gen_classes_define(classes)
puts gen_all_parser(classes)
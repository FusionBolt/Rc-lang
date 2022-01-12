require './ir/vm/generator'

module GenTest
  class Label < TypeStruct.new(:name)
  end

  # test sort by name
  class Add
  end

  class SetLocal < TypeStruct.new(:offset => :int)
  end
end

describe 'generator' do
  before do
    @classes = get_classes(GenTest)
  end
  context 'get_classes' do
    it 'succeed' do
      [GenTest::Add, GenTest::Label, GenTest::SetLocal].each do |klass|
        expect(@classes.include? klass)
      end
    end
  end

  # todo:sort by const name
  context 'enum inst type' do
    it 'succeed' do
      s = <<SRC
enum class InstType {
Add,
Label,
SetLocal,
};
SRC
      expect(gen_enum_inst_type(@classes)).to eq s
    end
  end

  context 'class define' do
    before do
      @add = <<SRC
struct Add : VMInst
{
public:
  Add():VMInst(InstType::Add) {}


};
SRC
      @label = <<SRC
struct Label : VMInst
{
public:
  Label(string _name):VMInst(InstType::Label), name(_name) {}

string name;
};
SRC
      @set_local = <<SRC
struct SetLocal : VMInst
{
public:
  SetLocal(int _offset):VMInst(InstType::SetLocal), offset(_offset) {}

int offset;
};
SRC
    end
    context 'single class' do
      it 'empty class' do
        expect(gen_class_define(GenTest::Add)).to eq @add
      end

      it 'normal type struct class' do
        expect(gen_class_define(GenTest::Label)).to eq @label
      end

      it 'spec type struct class' do
        expect(gen_class_define(GenTest::SetLocal)).to eq @set_local
      end
    end
  end
end
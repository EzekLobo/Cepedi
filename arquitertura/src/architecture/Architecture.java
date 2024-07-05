package architecture;

import components.Bus;
import components.Demux;
import components.Memory;
import components.Register;
import components.Ula;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Architecture {

  private final boolean simulation;
  private boolean halt;

  private Bus intbus;
  private Bus extbus;

  private Register PC;
  private Register IR;
  private Register RPG;
  private Register RPG1;
  private Register RPG2;
  private Register RPG3;
  private Register flags;
  private Demux demux;

  private Ula ula;

  private Memory statusMemory;
  private Memory memory;
  private int memorySize;

  private ArrayList<String> commandsList;
  private ArrayList<Register> registersList;

  
  private void componentsInstances() {
    
    extbus = new Bus();
    intbus = new Bus();

    
    PC = new Register("PC", intbus, intbus);
    IR = new Register("IR", extbus, intbus);
    RPG = new Register("RPG0", extbus, extbus);
    RPG1 = new Register("RPG1", extbus, extbus);
    RPG2 = new Register("RPG2", extbus, extbus);
    RPG3 = new Register("RPG3", extbus, extbus);

    flags = new Register(2, extbus);
    fillRegistersList();

 
    ula = new Ula(extbus, intbus);
    statusMemory = new Memory(2, extbus);

    memorySize = 128;
    memory = new Memory(memorySize, extbus);

    demux = new Demux();

    fillCommandsList();
  }

  /**
   * This method fills the registers list inserting into them all the registers we have.
   * IMPORTANT!
   * The first register to be inserted must be the default RPG
   */
  private void fillRegistersList() {
    registersList = new ArrayList<>();
    registersList.add(RPG); 
    registersList.add(RPG1);
    registersList.add(RPG2);
    registersList.add(RPG3);
    registersList.add(IR);
    registersList.add(PC);
    registersList.add(flags);
  }

  /**
   * Constructor that instanciates all components according the architecture diagram
   *
   * @param simulation Set the simulation mode
   */
  public Architecture(boolean simulation) {
    componentsInstances();
    this.simulation = simulation;
  }

  public Architecture() {
    this(false);
  }

  /*
   * Getters
   */
  public Bus getExtbus() {
    return extbus;
  }

  public Bus getIntbus() {
    return intbus;
  }

  public Register getIR() {
    return IR;
  }

  public Register getPC() {
    return PC;
  }

  public Register getRPG() {
    return RPG;
  }

  public Register getRPG1() {
    return RPG1;
  }

  public Register getRPG2() {
    return RPG2;
  }

  public Register getRPG3() {
    return RPG3;
  }

  public Register getFlags() {
    return flags;
  }

  public Demux getDemux() {
    return demux;
  }

  public Ula getUla() {
    return ula;
  }

  public Memory getStatusMemory() {
    return statusMemory;
  }

  public Memory getMemory() {
    return memory;
  }

  public ArrayList<Register> getRegistersList() {
    return registersList;
  }

  public ArrayList<String> getCommandsList() {
    return commandsList;
  }

  public int getMemorySize() {
    return memorySize;
  }

 
  protected void fillCommandsList() {
    commandsList = new ArrayList<>();
    	commandsList.add("addRegReg"); //0
		commandsList.add("addMemReg"); //1
		commandsList.add("addRegMem"); //2
		commandsList.add("addImmReg"); //3
		commandsList.add("subRegReg"); //4
		commandsList.add("subMemReg"); //5
		commandsList.add("subRegMem"); //6
		commandsList.add("subImmReg"); //7
		commandsList.add("imulMemReg"); //8 NAO FEITO
		commandsList.add("imulRegMem"); //9 FEITO ERRADO
		commandsList.add("imulRegReg"); //10 NAO FEITO
		commandsList.add("moveMemReg"); //11
		commandsList.add("moveRegMem"); //12
		commandsList.add("moveRegReg"); //13
		commandsList.add("moveImmReg"); //14
		commandsList.add("incReg"); //15
		commandsList.add("jmp"); //16
		commandsList.add("jn"); //17
		commandsList.add("jz"); //18
		commandsList.add("jeq"); //19
		commandsList.add("jneq"); //20
		commandsList.add("jgt"); //21
		commandsList.add("jlw"); //22
  }

  /**
   * This method is used after some ULA operations, setting the flags bits according the result.
   *
   * @param result is the result of the operation
   *               NOT TESTED!!!!!!!
   */
  public void setStatusFlags(int result) {
    flags.setBit(0, 0);
    flags.setBit(1, 0);
    if (result == 0) { // bit 0 in flags must be 1 in this case
      flags.setBit(0, 1);
    }
    if (result < 0) { // bit 1 in flags must be 1 in this case
      flags.setBit(1, 1);
    }
  }
  public void addRegReg() {
    // Increment PC to point to the first register
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();

    // Read the first register id from the memory
    memory.read();

    // Select the first register and read it
    demux.setValue(extbus.get());
    registersRead();

    // Wait for the value of the second register
    ula.store(0);

    // Increment PC to point to the second register
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();

    // Read the second register id from the memory
    memory.read();

    // Select the second register and read it
    demux.setValue(extbus.get());
    registersRead();

    // Save the value of the second register in the ula
    ula.store(1);

    // Add the value of the first register to the value of the second register
    ula.add();

    // Move the value from the PC to the extbus
    ula.internalStore(0);
    ula.read(0);

    // Write the value from the ula to the selected register
    memory.read();
    demux.setValue(extbus.get());
    ula.read(1);
    registersStore();
    setStatusFlags(extbus.get()); // Set the flags according the result

    // Increment PC to point to the next command
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    PC.internalStore();
  }

  public void addMemReg() {
    // Increment PC to point to the first register
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();

    // Read the value from the memory
    memory.read();
    memory.read();

    // Wait for the value of the second register
    ula.store(0);

    // Increment PC to point to the second register
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();

    // Read the second register id from the memory
    memory.read();

    // Select the second register and read it
    demux.setValue(extbus.get());
    registersRead();

    // Save the value of the second register in the ula
    ula.store(1);

    // Add the value of the first register to the value of the second register
    ula.add();

    // Move the value from the PC to the extbus
    ula.internalStore(0);
    ula.read(0);

    // Write the value from the ula to the selected register
    memory.read();
    demux.setValue(extbus.get());
    ula.read(1);
    registersStore();
    setStatusFlags(extbus.get()); // Set the flags according the result

    // Increment PC to point to the next command
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    PC.internalStore();
  }

  public void addRegMem() {
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();
    memory.read();
    demux.setValue(extbus.get());
    registersRead();
    ula.store(0);
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();
    memory.read();
    memory.store();
    memory.read();
    ula.store(1);
    ula.add();
    ula.read(1);
    setStatusFlags(extbus.get()); 
    memory.store();
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();
  }

   public void addImmReg() {
    // Increment PC to point to the immediate value
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();
  
    // Read the immediate value from the memory
    memory.read();
    ula.store(0);
  
    // Increment PC to point to the register id
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();
  
    // Read the register id from the memory
    memory.read();
    demux.setValue(extbus.get());
  
    // Select the register and read its value
    registersRead();
    ula.store(1);
  
    // Add the immediate value to the register value
    ula.add();
  
    // Write the result back to the register
    ula.read(1);
    demux.setValue(extbus.get());
    registersStore();
    setStatusFlags(extbus.get());
  
    // Increment PC to point to the next command
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    PC.internalStore();
  }

  public void subRegReg() {
    // Increment PC to point to the first register
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();

    // Read the first register id from the memory
    memory.read();

    // Select the first register and read it
    demux.setValue(extbus.get());
    registersRead();

    // Wait for the value of the second register
    ula.store(0);

    // Increment PC to point to the second register
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();

    // Read the second register id from the memory
    memory.read();

    // Select the second register and read it
    demux.setValue(extbus.get());
    registersRead();

    // Save the value of the second register in the ula
    ula.store(1);

    // Subtracts the value of the first register to the value of the second register
    ula.sub();

    // Move the value from the PC to the extbus
    ula.internalStore(0);
    ula.read(0);

    // Write the value from the ula to the selected register
    memory.read();
    demux.setValue(extbus.get());
    ula.read(1);
    registersStore();
    setStatusFlags(extbus.get()); // Set the flags according the result

    // Increment PC to point to the next command
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    PC.internalStore();
  }

  
  public void subMemReg() {
    // Increment PC to point to the first register
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();

    // Read the first register id from the memory
    memory.read();
    memory.read();

    // Wait for the value of the second register
    ula.store(0);

    // Increment PC to point to the second register
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();

    // Read the second register id from the memory
    memory.read();

    // Select the second register and read it
    demux.setValue(extbus.get());
    registersRead();

    // Save the value of the second register in the ula
    ula.store(1);

    // Subtracts the value of the first register to the value of the second register
    ula.sub();

    // Move the value from the PC to the extbus
    ula.internalStore(0);
    ula.read(0);

    // Write the value from the ula to the selected register
    memory.read();
    demux.setValue(extbus.get());
    ula.read(1);
    registersStore();
    setStatusFlags(extbus.get()); // Set the flags according the result

    // Increment PC to point to the next command
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    PC.internalStore();
  }

 
  public void subRegMem() {
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();
    memory.read();
    demux.setValue(extbus.get());
    registersRead();
    ula.store(0);
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();
    memory.read();
    memory.store();
    memory.read();
    ula.store(1);
    ula.sub();
    ula.read(1);
    setStatusFlags(extbus.get()); // Set the flags according the result
    memory.store();
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();
  }


  public void subImmReg() {
    // Increment PC to point to the immediate value
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();
  
    // Read the immediate value from the memory
    memory.read();
    ula.store(0);
  
    // Increment PC to point to the register id
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();
  
    // Read the register id from the memory
    memory.read();
    demux.setValue(extbus.get());
  
    // Select the register and read its value
    registersRead();
    ula.store(1);
  
    // Subtract the immediate value from the register value
    ula.sub();
  
    // Write the result back to the register
    ula.read(1);
    demux.setValue(extbus.get());
    registersStore();
    setStatusFlags(extbus.get());
  
    // Increment PC to point to the next command
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    PC.internalStore();
  }

 
  public void imulMemReg() {
    
}

 
  public void ImulRegMem() {
	  
	}
  
  public void imulRegReg() {
     
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();	

  }


  public void moveMemReg() {
    // Increment PC to point to the memory position
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();

    memory.read(); // Get the first parameter
    memory.read(); // Get the value from the memory position
    ula.store(0); // Save the value of the extbus in the ula

    // Increment PC to point to the register id
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();

    memory.read(); // Get the register id

    demux.setValue(extbus.get()); // Set the demux value to the id of the register
    ula.read(0); // Move the value from the ula to the extbus
    registersStore(); // Write the value from the extbus to the selected register

    // Increment PC to point to the memory position
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    PC.internalStore();
  }

  
  public void moveRegMem() {
    // Increment PC to point to the register
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();

    // Read the register id from the memory
    memory.read();
    ula.store(0); // storing the register id in the ula

    // Increment PC to point to the memory position
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();

    memory.read(); // Get the value of the memory position
    memory.store(); // Sends the memory position and waits for the value

    // Move the value from the ula to the extbus
    ula.read(0);

    // Write the value from the ula to the selected register
    demux.setValue(extbus.get());
    registersRead();

    // Write the value from the extbus to the memory
    memory.store();

    // Increment PC to point to the next command
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    PC.internalStore();
  }

  
  public void moveRegReg() {
    // Increment PC to point to the first register
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();

    // Read the register id from the memory
    memory.read();
    ula.store(0); // storing the register id in the ula

    // Increment PC to point to the second register
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    PC.internalStore();
    ula.read(0); // send the extbus value back to the extbus

    // Select the first register and read it
    demux.setValue(extbus.get());
    registersRead();

    // Save the value of the first register in the ula
    ula.store(0);

    // Move the value from the pc to the extbus
    PC.internalRead();
    ula.internalStore(1);
    ula.read(1);

    // Read the register id from the memory
    memory.read();

    // Select the second register
    demux.setValue(extbus.get());

    // Move the first register value that was stored in the ula to the extbus
    ula.read(0);

    // Write the value from the ula to the selected register
    registersStore();

    // Increment PC to point to the next command
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    PC.internalStore();


  }

void moveImmReg() {
    // Increment PC to point to the immediate value
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();

    // Read the immediate value from the memory
    memory.read();
    ula.store(0); // storing the immediate value in the ula

    // Increment PC to point to the register
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();

    // Read the register id from the memory
    memory.read();

    // Select the register
    demux.setValue(extbus.get());

    // Move the immediate value that was stored in the ula to the extbus
    ula.read(0);

    // Write the value from the ula to the selected register
    registersStore();

    // Increment PC to point to the next command
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    PC.internalStore();
  }

  
  public void incReg() {
    // Increment PC to point to the register
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();

    // Read the register id from the memory
    memory.read();

    // Select the register
    demux.setValue(extbus.get());
    registersRead();

    // Save the value of the selected register in the ula
    ula.store(1);

    // Increment the value in the ula
    ula.inc();

    // Move the value from the ula to the extbus
    ula.read(1);
    setStatusFlags(extbus.get()); // Set the flags according the result

    // Write the value from the ula to the selected register
    registersStore();

    // Increment PC to point to the next command
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    PC.internalStore();
  }

  
  public void jmp() {
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();

    memory.read();
    ula.store(0);
    ula.internalRead(0);
    PC.internalStore();
  }

 
  public void jn() {
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();

    memory.read();
    statusMemory.storeIn1();

    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();
    statusMemory.storeIn0();

    extbus.put(flags.getBit(1));
    statusMemory.read();

    ula.store(0);
    ula.internalRead(0);

    PC.internalStore();
  }

  public void jz() {
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();

    memory.read();
    statusMemory.storeIn1();

    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();
    statusMemory.storeIn0();

    extbus.put(flags.getBit(0));
    statusMemory.read();

    ula.store(0);
    ula.internalRead(0);

    PC.internalStore();
  }

 

  public void jeq() {
    // Increment PC to point to the first register
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();

    // Read the first register id from the memory
    memory.read();

    // Select the first register and read it
    demux.setValue(extbus.get());
    registersRead();

    // Wait for the value of the second register
    ula.store(0);

    // Increment PC to point to the second register
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();

    // Read the second register id from the memory
    memory.read();

    // Select the second register and read it
    demux.setValue(extbus.get());
    registersRead();

    // Save the value of the second register in the ula
    ula.store(1);

    // Subtracts the value of the first register to the value of the second register
    ula.sub();
    ula.read(1);
    setStatusFlags(extbus.get()); // Set the flags according the result

    // Increment PC to point to the memory position
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();

    // Read the memory position from the memory
    memory.read();

    statusMemory.storeIn1();

    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();

    statusMemory.storeIn0();

    extbus.put(flags.getBit(0));
    statusMemory.read();

    ula.store(0);
    ula.internalRead(0);
    PC.internalStore();
  }

  
  public void jgt() {
    // Increment PC to point to the first register
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();

    // Read the first register id from the memory
    memory.read();

    // Select the first register and read it
    demux.setValue(extbus.get());
    registersRead();

    // Wait for the value of the second register
    ula.store(0);

    // Increment PC to point to the second register
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();

    // Read the second register id from the memory
    memory.read();

    // Select the second register and read it
    demux.setValue(extbus.get());
    registersRead();

    // Save the value of the second register in the ula
    ula.store(1);

    // Subtracts the value of the first register to the value of the second register
    ula.sub();
    ula.read(1);
    setStatusFlags(extbus.get()); // Set the flags according the result

    // Increment PC to point to the memory position
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();

    // Read the memory position from the memory
    memory.read();

    statusMemory.storeIn1();

    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();

    statusMemory.storeIn0();

    // Check if it > and not >=
    extbus.put(flags.getBit(1));
    ula.store(0);
    extbus.put(flags.getBit(0));
    ula.store(1);
    ula.sub(); // If the result is 0, the first register is greater than the second and not equal
    ula.read(1);
    setStatusFlags(extbus.get());
    extbus.put(flags.getBit(0));
    statusMemory.read();

    ula.store(0);
    ula.internalRead(0);
    PC.internalStore();
  }

  
  public void jlw() {
    // Increment PC to point to the first register
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();

    // Read the first register id from the memory
    memory.read();

    // Select the first register and read it
    demux.setValue(extbus.get());
    registersRead();

    // Wait for the value of the second register
    ula.store(0);

    // Increment PC to point to the second register
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();

    // Read the second register id from the memory
    memory.read();

    // Select the second register and read it
    demux.setValue(extbus.get());
    registersRead();

    // Save the value of the second register in the ula
    ula.store(1);

    // Subtracts the value of the first register to the value of the second register
    ula.sub();
    ula.read(1);
    setStatusFlags(extbus.get()); // Set the flags according the result

    // Increment PC to point to the memory position
    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();

    // Read the memory position from the memory
    memory.read();

    statusMemory.storeIn1();

    PC.internalRead();
    ula.internalStore(1);
    ula.inc();
    ula.internalRead(1);
    ula.read(1);
    PC.internalStore();

    statusMemory.storeIn0();

    // Check if it > and not >=
    extbus.put(flags.getBit(1));
    ula.store(0);
    extbus.put(flags.getBit(0));
    ula.store(1);
    ula.sub(); // If the result is not negative and not zero, the first register is less than the second
    ula.read(1);
    setStatusFlags(extbus.get());

    extbus.put(flags.getBit(1));
    ula.store(0);
    extbus.put(flags.getBit(0));
    ula.store(1);
    ula.sub(); // If the result is 0, the first register is greater than the second and not equal
    ula.read(1);
    setStatusFlags(extbus.get());
    extbus.put(flags.getBit(0));
    statusMemory.read();

    ula.store(0);
    ula.internalRead(0);
    PC.internalStore();
  }

 
public void jneq() {
  // Increment PC to point to the first register
  PC.internalRead();
  ula.internalStore(1);
  ula.inc();
  ula.internalRead(1);
  ula.read(1);
  PC.internalStore();

  // Read the first register id from the memory
  memory.read();

  // Select the first register and read it
  demux.setValue(extbus.get());
  registersRead();

  // Store the value of the first register in ULA
  ula.store(0);

  // Increment PC to point to the second register
  PC.internalRead();
  ula.internalStore(1);
  ula.inc();
  ula.internalRead(1);
  ula.read(1);
  PC.internalStore();

  // Read the second register id from the memory
  memory.read();

  // Select the second register and read it
  demux.setValue(extbus.get());
  registersRead();

  // Store the value of the second register in ULA
  ula.store(1);

  // Subtract the value of the first register from the value of the second register
  ula.sub();
  ula.read(1);
  setStatusFlags(extbus.get()); // Set the flags according to the result

  // Increment PC to point to the memory position
  PC.internalRead();
  ula.internalStore(1);
  ula.inc();
  ula.internalRead(1);
  ula.read(1);
  PC.internalStore();

  // Read the memory position from the memory
  memory.read();

  // Store the memory position to jump to in statusMemory
  statusMemory.storeIn1();

  // Increment PC to point to the next instruction
  PC.internalRead();
  ula.internalStore(1);
  ula.inc();
  ula.internalRead(1);
  ula.read(1);
  PC.internalStore();

  // Store the memory position to jump to in statusMemory
  statusMemory.storeIn0();

  // Check if the result of the subtraction is not zero (i.e., the registers are not equal)
  extbus.put(flags.getBit(0));
  statusMemory.read();

  ula.store(0);
  ula.internalRead(0);
  PC.internalStore();
}



  /**
   * This method performs an (external) read from a register into the register list.
   * The register id must be in the demux bus
   */
  private void registersRead() {
    registersList.get(demux.getValue()).read();
  }

  /**
   * This method performs an (internal) read from a register into the register list.
   * The register id must be in the demux bus
   */
  private void registersInternalRead() {
    registersList.get(demux.getValue()).internalRead();
  }

  /**
   * This method performs an (external) store toa register into the register list.
   * The register id must be in the demux bus
   */
  private void registersStore() {
    registersList.get(demux.getValue()).store();
  }

  /**
   * This method performs an (internal) store toa register into the register list.
   * The register id must be in the demux bus
   */
  private void registersInternalStore() {
    registersList.get(demux.getValue()).internalStore();
  }

  /**
   * This method reads an entire file in machine code and
   * stores it into the memory
   * NOT TESTED
   *
   * @param filename the name of the file to be read
   * @throws IOException if the file is not found
   */
  public void readExec(String filename) throws IOException {
    BufferedReader br = new BufferedReader(new FileReader(filename + ".dxf"));
    String linha;
    int i = 0;
    while ((linha = br.readLine()) != null) {
      extbus.put(i);
      memory.store();
      extbus.put(Integer.parseInt(linha));
      memory.store();
      i++;
    }
    br.close();
  }

  /**
   * This method executes a program that is stored in the memory
   */
  public void controlUnitEexec() {
    halt = false;
    while (!halt) {
      fetch();
      decodeExecute();
    }
  }

  /**
   * This method implements the decoding process,
   * that is to find the correct operation do be executed
   * according the command.
   * And the executing process, that is the execution itself of the command
   */
  private void decodeExecute() {
    IR.internalRead(); // the instruction is in the internal bus
    int command = intbus.get();
    simulationDecodeExecuteBefore(command);

    switch (command) {
      case 0:
        addRegReg();
        break;
      case 1:
        addMemReg();
        break;
      case 2:
        addRegMem();
        break;
      case 3:
        addImmReg();
        break;
      case 4:
        subRegReg();
        break;
      case 5:
        subMemReg();
        break;
      case 6:
        subRegMem();
        break;
      case 7:
        subImmReg();
        break;
      case 8:
        imulMemReg();
        break;
      case 9:
        break;
      case 10:
        imulRegReg();
        break;
      case 11:
        moveMemReg();
        break;
      case 12:
        moveRegMem();
        break;
      case 13:
        moveRegReg();
        break;
      case 14:
        moveImmReg();
        break;
      case 15:
        incReg();
        break;
      case 16:
        jmp();
        break;
      case 17:
        jn();
        break;
      case 18:
        jz();
        break;
      case 19:
        jeq();
        break;
      case 20:
        jneq();
        break;
      case 21:
        jgt();
        break;
      case 22:
        jlw();
        break;
      default:
        halt = true;
        break;
      }

    if (simulation) {
      simulationDecodeExecuteAfter();
    }
  }

  /**
   * This method is used to show the components status in simulation conditions
   * NOT TESTED
   *
   * @param command the command to be executed
   */
  private void simulationDecodeExecuteBefore(int command) {
    System.out.println("----------BEFORE Decode and Execute phases--------------");
    String instruction;
    int parameter = 0;
    for (Register r : registersList) {
      System.out.println(r.getRegisterName() + ": " + r.getData());
    }
    if (command != -1) {
      instruction = commandsList.get(command);
    } else {
      instruction = "END";
    }

    int operands = getOperandSize(instruction);
    System.out.print("Instruction: " + instruction);
    for (int i = 0; i < operands; i++) {
      parameter = memory.getDataList()[PC.getData() + i + 1];
      System.out.print(" " + parameter);
    }
    System.out.println();

    if ("read".equals(instruction)) {
      System.out.println("memory[" + parameter + "]=" + memory.getDataList()[parameter]);
    }
  }

  /**
   * This method is used to show the components status in simulation conditions
   * NOT TESTED
   */
  private void simulationDecodeExecuteAfter() {
    String instruction;
    System.out.println("-----------AFTER Decode and Execute phases--------------");
    System.out.println("Internal Bus 1: " + intbus.get());
    System.out.println("Internal Bus 2: " + intbus.get());
    System.out.println("External Bus 1: " + extbus.get());
    for (Register r : registersList) {
      System.out.println(r.getRegisterName() + ": " + r.getData());
    }
    Scanner entrada = new Scanner(System.in);
    System.out.println("Press <Enter>");
    String mensagem = entrada.nextLine();
  }

  /**
   * This method uses PC to find, in the memory,
   * the command code that must be executed.
   * This command must be stored in IR
   * NOT TESTED!
   */
  private void fetch() {
    PC.internalRead();
    ula.internalStore(0);
    ula.read(0);
    memory.read();
    IR.store();
    simulationFetch();
  }

  /**
   * This method is used to show the components status in simulation conditions
   * NOT TESTED!!!!!!!!!
   */
  private void simulationFetch() {
    if (simulation) {
      System.out.println("-------Fetch Phase------");
      System.out.println("PC: " + PC.getData());
      System.out.println("IR: " + IR.getData());
    }
  }

  /**
   * This method is used to show in a correct way the operands (if there is any) of instruction,
   * when in simulation mode
   * NOT TESTED!!!!!
   *
   * @param instruction the instruction to be executed
   * @return the number of operands
   */
  private int getOperandSize(String instruction) {
    if (instruction.equals("END")) {
      return 0;
    }

    int operands = 0;
    for (int i = 0; i < instruction.length(); i++) {
      // check if the character is a capital letter
      if (Character.isUpperCase(instruction.charAt(i))) {
        operands++;
      }
    }
    return operands;
  }

  public static void main(String[] args) throws IOException {
    Architecture arch = new Architecture(true);
    arch.readExec("program");
    arch.controlUnitEexec();
  }
}
import ConfigurationForm, {
	ConfigurationFormSchema,
	type ConfigurationFormValues,
} from "@/components/config-panel/configuration-form";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
	Card,
	CardContent,
	CardDescription,
	CardHeader,
	CardTitle,
} from "@/components/ui/card";
import {
	Dialog,
	DialogClose,
	DialogContent,
	DialogDescription,
	DialogFooter,
	DialogHeader,
	DialogTitle,
	DialogTrigger,
} from "@/components/ui/dialog";
import { Form } from "@/components/ui/form";
import { Separator } from "@/components/ui/separator";
import { useStomp } from "@/hooks/use-stomp";
import { agentTemplates, useStore } from "@/hooks/use-store";
import { cn } from "@/lib/utils";
import { zodResolver } from "@hookform/resolvers/zod";
import {
	AlertCircle,
	CheckCircle,
	Cpu,
	Home,
	Play,
	Plus,
	Radio,
	Settings,
	Users,
	Wrench,
} from "lucide-react";
import { useFieldArray, useForm } from "react-hook-form";
import { toast } from "sonner";

const defaultValues: ConfigurationFormValues = {
	simulationStartTime: new Date(),
	maxPowerCapacity: 500,
	credits: 1000,
	areas: [
		{
			name: "Kitchen",
			attributes: {
				temperature: 20,
			},
			agents: agentTemplates.map((template) => ({
				templateId: template.name,
				count: 0,
			})),
		},
	],
};

const ConfigPanel = () => {
	const { areas, agents, setSystemStatus } = useStore();
	const { isConnected } = useStomp();
	const hasConfiguration = areas.length > 0 && agents.length > 0;
	const totalAgents = agents.length;

	const form = useForm<ConfigurationFormValues>({
		resolver: zodResolver(ConfigurationFormSchema),
		defaultValues: defaultValues,
	});

	const fieldArray = useFieldArray({
		control: form.control,
		name: "areas",
	});

	async function handleStartSystem() {
		// Get the current form values to include the additional fields
		const formValues = form.getValues();

		try {
			setSystemStatus("starting");

			if (!isConnected) {
				toast.error("WebSocket connection not available");
				setSystemStatus("stopped");
				return;
			}

			// Send configuration with all form fields
			const configResponse = await fetch("/system/config", {
				method: "POST",
				headers: {
					"Content-Type": "application/json",
				},
				body: JSON.stringify({
					// Include the additional form fields
					simulationStartTime:
						formValues.simulationStartTime?.toISOString(),
					maxPowerCapacity: formValues.maxPowerCapacity,
					credits: formValues.credits,
					// Existing areas and agents
					areas: areas.map((area) => ({
						name: area.name.toLowerCase(),
						attributes: area.attributes,
					})),
					agents: agents.map((agent) => ({
						aid: agent.aid,
						name: agent.name,
						area: agent.area.toLowerCase(),
					})),
				}),
			});

			if (!configResponse.ok) {
				toast.error("Failed to upload the configuration");
				setSystemStatus("stopped");
				return;
			}

			const response = await fetch("/system/start", {
				method: "POST",
			});

			if (!response.ok) {
				toast.error("Failed to start the system");
				setSystemStatus("stopped");
				return;
			}

			setSystemStatus("running");
			toast.success("System started successfully");
		} catch (error) {
			console.error("Error starting system:", error);
			toast.error("Failed to start the system");
			setSystemStatus("stopped");
		}
	}

	return (
		<main className="flex-1 p-4 overflow-y-auto flex flex-col justify-center">
			<div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
				{/* Configuration Panel */}
				<Card className="h-fit">
					<CardHeader>
						<CardTitle className="flex items-center">
							<Settings className="size-5 mr-2" />
							System Configuration
						</CardTitle>
						<CardDescription>
							Set up your rooms and agents before starting the
							simulation
						</CardDescription>
					</CardHeader>
					<CardContent className="flex flex-col gap-4">
						<div className="grid grid-cols-2 gap-4">
							<div className="text-center p-4 bg-blue-600/10 rounded-lg">
								<Home className="size-8 text-blue-800 dark:text-blue-600 mx-auto mb-2" />
								<div className="text-2xl font-bold text-blue-800 dark:text-blue-600">
									{areas.length}
								</div>
								<div className="text-sm text-blue-700">
									Rooms
								</div>
							</div>
							<div className="text-center p-4 bg-green-500/10 rounded-lg">
								<Users className="size-8 text-green-800 dark:text-green-600 mx-auto mb-2" />
								<div className="text-2xl font-bold text-green-800 dark:text-green-600">
									{totalAgents}
								</div>
								<div className="text-sm text-green-700">
									Agents
								</div>
							</div>
						</div>

						<Separator />

						{hasConfiguration ? (
							<div className="flex flex-col gap-3">
								<div className="flex items-center text-green-600">
									<CheckCircle className="h-4 w-4 mr-2" />
									<span className="font-medium">
										Configuration Ready
									</span>
								</div>
								<div className="text-sm text-muted-foreground">
									Your smart home is configured with{" "}
									{areas.length} rooms and {totalAgents}{" "}
									agents.
								</div>
							</div>
						) : (
							<div className="flex flex-col gap-3">
								<div className="flex items-center text-amber-600">
									<AlertCircle className="h-4 w-4 mr-2" />
									<span className="font-medium">
										Configuration Required
									</span>
								</div>
								<div className="text-sm text-muted-foreground">
									Please configure your rooms and agents
									before starting the system.
								</div>
							</div>
						)}
						<Dialog>
							<DialogTrigger asChild>
								<Button variant="outline" className="w-full">
									<Wrench className="mr-2 h-4 w-4" />
									{hasConfiguration
										? "Modify Configuration"
										: "Configure System"}
								</Button>
							</DialogTrigger>
							<DialogContent className="max-w-dvw max-h-[90dvh] min-w-fit">
								<DialogHeader>
									<DialogTitle>
										<Settings className="inline mr-2 h-5 w-5" />
										System Configuration
									</DialogTitle>
									<DialogDescription>
										Configure your smart home rooms and
										agents. You can add, edit, or remove
										rooms and agents as needed.
									</DialogDescription>
								</DialogHeader>
								<Form {...form}>
									<ConfigurationForm
										fieldArray={fieldArray}
									/>
									<DialogFooter className="flex justify-between">
										<Button
											type="button"
											variant="outline"
											onClick={() =>
												fieldArray.append({
													name: "",
													attributes: {
														temperature: 21.0,
													},
													agents: agentTemplates.map(
														(template) => ({
															templateId:
																template.name,
															count: 0,
														})
													),
												})
											}
										>
											<Plus className="mr-2 h-4 w-4" />
											Add Room
										</Button>
										{form.formState.isValid ? (
											<DialogClose asChild>
												<Button
													type="submit"
													form="configuration-form"
												>
													Save Configuration
												</Button>
											</DialogClose>
										) : (
											<Button
												type="submit"
												form="configuration-form"
											>
												Save Configuration
											</Button>
										)}
									</DialogFooter>
								</Form>
							</DialogContent>
						</Dialog>
					</CardContent>
				</Card>

				{/* Start System Panel */}
				<Card>
					<CardHeader>
						<CardTitle className="flex items-center">
							<Play className="h-5 w-5 mr-2" />
							System Control
						</CardTitle>
						<CardDescription>
							Start the smart home simulation with your current
							configuration
						</CardDescription>
					</CardHeader>
					<CardContent className="flex flex-col gap-4">
						<div className="p-4 bg-muted rounded-lg">
							<h4 className="font-medium mb-2">
								Pre-Start Checklist:
							</h4>
							<div className="flex flex-col gap-2 text-sm">
								<div
									className={`flex items-center ${
										hasConfiguration
											? "text-green-600"
											: "text-muted-foreground"
									}`}
								>
									<CheckCircle className="h-4 w-4 mr-2" />
									Configuration complete
								</div>
								<div
									className={`flex items-center ${
										isConnected
											? "text-green-600"
											: "text-muted-foreground"
									}`}
								>
									<CheckCircle className="h-4 w-4 mr-2" />
									WebSocket connection available
								</div>
							</div>
						</div>

						<Button
							className="w-full h-12 text-lg"
							disabled={!hasConfiguration}
							onClick={handleStartSystem}
						>
							<Play className="h-5 w-5 mr-2" />
							Start Smart Home System
						</Button>

						{!hasConfiguration && (
							<p className="text-sm text-amber-600 text-center">
								Complete your configuration first to enable
								system start
							</p>
						)}
					</CardContent>
				</Card>
			</div>

			{/* Configuration Overview */}
			{hasConfiguration && (
				<Card className="mt-8">
					<CardHeader>
						<CardTitle>Configuration Overview</CardTitle>
						<CardDescription>
							Review your current smart home setup
						</CardDescription>
					</CardHeader>
					<CardContent>
						<div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
							{areas.map((area) => {
								const areaAgents = agents.filter(
									(agent) => agent.area === area.name
								);
								return (
									<div
										key={area.name}
										className="border rounded-lg p-4"
									>
										<div className="flex items-center mb-3">
											<Home className="h-5 w-5 text-blue-600 mr-2" />
											<h3 className="font-semibold">
												{area.name}
											</h3>
											<Badge
												variant="secondary"
												className="ml-auto"
											>
												{areaAgents.length} agents
											</Badge>
										</div>
										<div className="flex flex-col gap-3">
											{areaAgents
												.slice(0, 3)
												.map((agent) => (
													<div
														key={agent.aid}
														className="flex items-center justify-between text-sm"
													>
														<div className="flex items-center">
															{agent.type ===
															"appliance" ? (
																<Cpu className="size-4 text-blue-600" />
															) : (
																<Radio className="size-4 text-green-600" />
															)}
															<span className="ml-2">
																{agent.name}
															</span>
														</div>
														<Badge
															className={cn(
																"ml-2 text-xs",
																agent?.type ===
																	"sensor"
																	? "bg-blue-500/20 text-blue-800 dark:text-blue-400 dark:border-blue-400"
																	: "bg-green-500/20 text-green-800 dark:text-green-400 dark:border-green-400"
															)}
														>
															{agent?.type
																? agent.type
																		.charAt(
																			0
																		)
																		.toUpperCase() +
																  agent.type.slice(
																		1
																  )
																: ""}
														</Badge>
													</div>
												))}
											{areaAgents.length > 3 && (
												<div className="text-xs text-muted-foreground">
													+{areaAgents.length - 3}{" "}
													more agents
												</div>
											)}
										</div>
									</div>
								);
							})}
						</div>
					</CardContent>
				</Card>
			)}
		</main>
	);
};
export default ConfigPanel;
